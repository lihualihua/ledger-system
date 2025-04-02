package com.digital.doc.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.digital.common.core.exception.ApplicationException;
import com.digital.doc.helper.FileHelper;
import com.digital.doc.helper.UserPermissionHelper;
import com.digital.doc.service.IDocFileService;
import com.digital.doc.service.IUploadService;
import com.digital.model.doc.dto.DocFileDto;
import com.digital.model.doc.entity.DocFileEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional(rollbackFor = Exception.class)
public class UploadServiceImpl implements IUploadService {
    private static final Logger LOGGER = LogManager.getLogger(UploadServiceImpl.class);

    /**
     * 用于在并发环境下安全地跟踪上传进度
     */
    private static final ConcurrentHashMap<String, Integer> uploadProgress = new ConcurrentHashMap<>();

    @Autowired
    private IDocFileService fileService;

    @Autowired
    private UserPermissionHelper userPermissionHelper;

    @Autowired
    private FileHelper fileHelper;

    @Override
    public DocFileEntity upload(MultipartFile file, DocFileDto fileDto) {
        Integer ownership = fileDto.getOwnership();
        String folderPath = fileHelper.getFolderPath(ownership);
        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            throw new ApplicationException("服务器处理异常, 文件名获取失败!");
        }
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        String uuidFileName= IdUtil.fastSimpleUUID() + "." + fileType;
        long fileSize = file.getSize();
        String filePath = folderPath + uuidFileName;
        DocFileEntity docFileEntity = null;
        try {
            FileUtil.writeBytes(file.getBytes(), filePath);
            fileDto.setFileName(fileName);
            fileDto.setFileType(fileType);
            fileDto.setFileSize(fileSize);
            fileDto.setFilePath(filePath);
            fileDto.setFolderFlag(0);
            docFileEntity = buildUploadFileEntity(fileDto);
            docFileEntity = fileService.createFile(docFileEntity, fileDto.getDeptId());
        } catch (IOException ex) {
            LOGGER.error(ex);
            throw new ApplicationException("服务器处理异常, 文件上传失败!");
        }

        return docFileEntity;
    }

    private DocFileEntity buildUploadFileEntity(DocFileDto fileDto) {
        DocFileEntity docFileEntity = new DocFileEntity();
        Integer ownership = fileDto.getOwnership();
        Long folderId = fileDto.getParentId();
        String fileType = fileDto.getFileType();
        Long fileSize = fileDto.getFileSize();
        String fileName = fileService.generateFileName(fileDto);
        docFileEntity.setFileName(fileName);
        docFileEntity.setFileType(fileType);
        docFileEntity.setFileSize(fileSize);
        docFileEntity.setFileSizeStr(FileUtil.readableFileSize(fileSize));
        docFileEntity.setFilePath(fileDto.getFilePath());
        docFileEntity.setDelFlag(1);
        docFileEntity.setOwnership(ownership);
        docFileEntity.setParentId(folderId);
        docFileEntity.setIcon(fileHelper.getFileIcon(fileDto.getIcon(), fileType));
        docFileEntity.setDownloadCount(0);
        docFileEntity.setPreviewCount(0);
        docFileEntity.setCollectCount(0);
        docFileEntity.setFolderFlag(0);
        docFileEntity.setEditFlag(userPermissionHelper.getEditFlag(null, ownership, folderId));
        return docFileEntity;
    }

    @Override
    public DocFileEntity uploadChunk(MultipartFile file, int chunkIndex, int totalChunks, DocFileDto fileDto) {
        Integer ownership = fileDto.getOwnership();
        String folderPath = fileHelper.getFolderPath(ownership);

        String fileName = fileDto.getFileName();
        if (StringUtils.isBlank(fileName)) {
            throw new ApplicationException("服务器处理异常, 文件名 {} 获取失败!", fileName);
        }
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        String filePath = folderPath + fileName;

        //分片路径
        File chunkFile = new File(filePath + "_" + chunkIndex);
        try {
            FileUtil.writeBytes(file.getBytes(), chunkFile);
        } catch (IOException e) {
            LOGGER.info("分片路径数据写入失败！ chunkFile: {}", chunkFile);
            throw new RuntimeException(e);
        }

        // 更新分片进度
        uploadProgress.merge(fileName, 1, Integer::sum);

        DocFileEntity docFileEntity = null;
        // 检查是否已上传所有分片
        if (uploadProgress.get(fileName) == totalChunks) {
            mergeChunks(file, filePath, totalChunks);
            // 清理分片文件
            uploadProgress.remove(fileName);
            // 插入表数据 doc_file_t
            fileDto.setOwnership(ownership);
            fileDto.setFileName(fileName);
            fileDto.setFilePath(filePath);
            fileDto.setFileType(fileType);
            fileDto.setFolderFlag(0);
            docFileEntity = buildUploadFileEntity(fileDto);
            // 插入表数据 doc_dept_file_t
            docFileEntity = fileService.createFile(docFileEntity, fileDto.getDeptId());

            //TODO:智能搜索创建ES索引（勿删）
           /* if (Objects.nonNull(docFileEntity)) {
                fileDealComp.uploadESByFile(docFileEntity);
            }*/
        }
        return docFileEntity;
    }

    // 合并分片
    private void mergeChunks(MultipartFile file, String filePath, int totalChunks) {
        File outputFile = new File(filePath);
        // 写入数据
        try {
            FileUtil.writeBytes(file.getBytes(), outputFile);
        } catch (IOException e) {
            LOGGER.info("合并分片写入数据失败！outputFile：{}", outputFile);
            throw new RuntimeException(e);
        }
        try (OutputStream out = Files.newOutputStream(outputFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (int i = 0; i < totalChunks; i++) {
                File chunkFile = new File(filePath + "_" + i);
                try (InputStream in = Files.newInputStream(chunkFile.toPath())) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                // 合并后删除分片文件
                chunkFile.delete();
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
            throw new ApplicationException("服务器处理异常, 文件上传失败!");
        }
    }

    @Override
    public DocFileEntity  folderUpload(MultipartFile file, int chunkIndex, int totalChunks, DocFileDto fileDto) {
        Integer ownership = fileDto.getOwnership();
        String folderPath = fileHelper.getFolderPath(ownership);

        String fileName = fileDto.getFileName();
        if (StringUtils.isBlank(fileName)) {
            throw new ApplicationException("服务器处理异常, 文件夹名 {} 获取失败!", fileName);
        }
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        String filePath = folderPath + fileName;

        //分片路径
        File chunkFile = new File(filePath + "_" + chunkIndex);
        try (FileOutputStream fos = new FileOutputStream(chunkFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            LOGGER.info("上传文件夹，分片路径数据写入失败！ chunkFile: {}", chunkFile);
            throw new RuntimeException(e);
        }

        // 更新分片进度
        uploadProgress.merge(fileName, 1, Integer::sum);

        DocFileEntity docFileEntity = null;
        // 检查是否已上传所有分片
        if (uploadProgress.get(fileName) == totalChunks) {
            try {
                mergeFileParts(fileName, filePath, totalChunks);
            } catch (IOException e) {
                LOGGER.info("上传文件夹，合并分片失败！ fileName: {}", fileName);
                throw new RuntimeException(e);
            }
            // 清理分片文件
            uploadProgress.remove(fileName);
            // 插入表数据 doc_file_t
            fileDto.setOwnership(ownership);
            fileDto.setFileName(fileName);
            fileDto.setFilePath(filePath);
            fileDto.setFileType(fileType);
            fileDto.setFolderFlag(0);
            docFileEntity = buildUploadFileEntity(fileDto);
            // 插入表数据 doc_dept_file_t
            docFileEntity = fileService.createFile(docFileEntity, fileDto.getDeptId());
        }
        return docFileEntity;
    }

    private void mergeFileParts(String fileName, String filePath, int totalChunks) throws IOException {
        File outputFile = new File(filePath);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            for (int i = 0; i < totalChunks; i++) {
                File partFile = new File(filePath, fileName + ".part" + i);
                Files.copy(partFile.toPath(), fos);
                partFile.delete();
            }
        }
    }

}
