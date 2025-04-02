package com.digital.doc.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.digital.common.core.util.R;
import com.digital.doc.helper.FileHelper;
import com.digital.doc.service.IUploadService;
import com.digital.model.doc.dto.DocFileDto;
import com.digital.model.doc.entity.DocFileEntity;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/doc/file")
@Tag(description = "doc", name = "文档上传")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@CrossOrigin
public class UploadController extends BaseController {

    @Autowired
    protected IUploadService uploadService;

    @Autowired
    private FileHelper fileHelper;

    /**
     * 单个上传文件
     *
     * @param file
     * @param ownership
     * @param parentId
     * @return
     */
    @PostMapping(value = "/upload/{ownership}/{parentId}")
    public R upload(@RequestPart("file") MultipartFile file, @PathVariable("ownership") Integer ownership,
                    @PathVariable("parentId") Long parentId, @RequestParam(value = "icon", required = false) String icon,
                    @RequestParam(value = "deptId", required = false) Long deptId) {

        checkOwnership(ownership);
        DocFileEntity folder = checkDocFolder(parentId);
        checkFile(file);
        Integer editFlag = null;
        if (Objects.nonNull(folder)) {
            editFlag = folder.getEditFlag();
        }
        checkUserRolePermission(editFlag, ownership);
        DocFileDto fileDto = getDocFileDto(ownership, parentId, icon, deptId);
        return R.ok(uploadService.upload(file, fileDto));
    }

    private static DocFileDto getDocFileDto(Integer ownership, Long parentId, String icon, Long deptId) {
        DocFileDto fileDto = new DocFileDto();
        fileDto.setOwnership(ownership);
        fileDto.setParentId(parentId);
        fileDto.setIcon(icon);
        fileDto.setDeptId(deptId);
        return fileDto;
    }

    /**
     * 批量上传
     *
     * @param files
     * @param ownership
     * @param parentId
     * @param icon
     * @return
     */
    @SneakyThrows
    @PostMapping(value = "/batchUpload/{ownership}/{parentId}")
    public R batchUpload(@RequestPart MultipartFile[] files, @PathVariable("ownership") Integer ownership,
                         @PathVariable("parentId") Long parentId, @RequestParam(value = "icon", required = false) String icon,
                         @RequestParam(value = "deptId", required = false) Long deptId,
                         @RequestParam(value = "tagId", required = false) Long tagId) {
        checkOwnership(ownership);
        DocFileEntity folder = checkDocFolder(parentId);
        Integer editFlag = null;
        if (Objects.nonNull(folder)) {
            editFlag = folder.getEditFlag();
        }
        checkUserRolePermission(editFlag, ownership);
        Map<String, List<DocFileEntity>> failedMap = new HashMap<>();
        List<DocFileEntity> failFileEntityList = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                checkFile(file);
                DocFileDto fileDto = getDocFileDto(ownership, parentId, icon, deptId);
                uploadService.upload(file, fileDto);
            } catch (Exception ex) {
                DocFileEntity docFileEntity = new DocFileEntity();
                docFileEntity.setFileName(file.getOriginalFilename());
                docFileEntity.setFilePath(file.getResource().getURI().getPath());
                failFileEntityList.add(docFileEntity);
            }
        }
        failedMap.put("failFileEntityList", failFileEntityList);
        return R.ok(failedMap);
    }

    /**
     *  分片上传
     *
     * @param file 文件
     * @param fileName 文件名
     * @param fileSize 文件总大小
     * @param chunkIndex 分片标记数（1-9..）
     * @param totalChunks 分片总数
     * @param ownership 文件归属1:公共，2:用户，3：部门
     * @param parentId 父级文件夹id ，根目录为0
     * @param icon 图标
     * @param deptId 部门id，doc_dept_file_t表或者当前用户部门id
     * @return R:DocFileEntity
     * @throws IOException IO异常信息
     */
    @PostMapping(value = "/shardUpload/{ownership}/{parentId}")
    public R shardUpload(@RequestPart("file") MultipartFile file,
                         @RequestParam(value = "fileName") String fileName,
                         @RequestParam(value = "fileSize") long fileSize,
                         @RequestParam(value = "chunkIndex") int chunkIndex,
                         @RequestParam(value = "totalChunks") int totalChunks,
                         @PathVariable("ownership") Integer ownership,
                         @PathVariable("parentId") Long parentId,
                         @RequestParam(value = "icon", required = false) String icon,
                         @RequestParam(value = "deptId", required = false) Long deptId) {
        checkOwnership(ownership);
        DocFileEntity folder = checkDocFolder(parentId);
        checkShardFile(fileName);
        Integer editFlag = null;
        if (Objects.nonNull(folder)) {
            editFlag = folder.getEditFlag();
        }
        checkUserRolePermission(editFlag, ownership);
        DocFileDto fileDto = getDocFileDto(ownership, parentId, icon, deptId);
        fileDto.setFileName(fileName);
        fileDto.setFileSize(fileSize);
        return R.ok(uploadService.uploadChunk(file, chunkIndex, totalChunks, fileDto), "分片上传成功！");
    }

    /**
     * 自定义上传
     *
     * @param ownership
     * @param parentId
     * @param icon
     * @param deptId
     * @param basePath
     * @return
     */
    @GetMapping(value = "/customUpload/{ownership}/{parentId}")
    public R customUpload(@PathVariable("ownership") Integer ownership, @PathVariable("parentId") Long parentId,
                          @RequestParam(value = "icon", required = false) String icon,
                          @RequestParam(value = "deptId", required = false) Long deptId,
                          @RequestParam(value = "basePath", required = false) String basePath) {
        File file = new File(basePath);
        if (!file.exists()) {
            return R.failed("文件目录不存在！");
        }
        File[] files = file.listFiles();
        if (files != null) {
            readFile(Arrays.asList(files), ownership, parentId, icon, deptId);
        }
        return R.ok();
    }

    private void readFile(List<File> files, Integer ownership, Long parentId, String icon, Long deptId) {
        if (Objects.isNull(files)) {
            return;
        }
        files.forEach(file -> {
            String fileName = file.getName();
            String filePath = file.getPath();
            // 解析文件入库
            if (file.isFile()) {
                String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
                long fileSize = file.length();
                DocFileEntity entity = new DocFileEntity();
                entity.setOwnership(ownership);
                entity.setEditFlag(1);
                entity.setParentId(parentId);
                entity.setFolderFlag(0);
                entity.setFileName(fileName);
                entity.setFilePath(filePath);
                entity.setFileSize(fileSize);
                entity.setFileType(fileType);
                entity.setCollectCount(0);
                entity.setPreviewCount(0);
                entity.setDownloadCount(0);
                entity.setFileSizeStr(FileUtil.readableFileSize(fileSize));
                entity.setIcon(fileHelper.getFileIcon(icon, fileType));
                entity.setDelFlag(1);
                entity.setCreateById(1L);
                entity.setCreateBy("admin");
                entity.setUpdateBy("admin");
                entity.setCreateTime(DateUtil.toLocalDateTime(new Date()));
                entity.setUpdateTime(DateUtil.toLocalDateTime(new Date()));
                docFileService.createFile(entity, deptId);
                return;
            }

            // 如果是文件夹，继续往下读取
            if (file.isDirectory()) {
                DocFileDto fileDto = createFolder(parentId, icon, deptId, fileName, 1, 1);
                File[] childFiles = file.listFiles();
                if (childFiles != null) {
                    readFile(Arrays.asList(childFiles), ownership, fileDto.getFileId(), icon, deptId);
                }
            }
        });
    }

    private DocFileDto createFolder(Long parentId, String icon, Long deptId, String fileName, Integer ownership, Integer editFlag) {
        DocFileDto fileDto = buildDocFileDto(parentId, icon, deptId, fileName, ownership, editFlag);
        return docFileService.createFolder(fileDto);
    }

    private DocFileDto buildDocFileDto(Long parentId, String icon, Long deptId, String fileName, Integer ownership, Integer editFlag) {
        DocFileDto fileDto = new DocFileDto();
        fileDto.setOwnership(ownership);
        fileDto.setParentId(parentId);
        fileDto.setFolderFlag(1);
        fileDto.setDelFlag(1);
        fileDto.setFileName(fileName);
        fileDto.setEditFlag(editFlag);
        fileDto.setDeptId(deptId);
        fileDto.setIcon(fileHelper.getFileIcon(icon, "folder"));
        return fileDto;
    }

    /**
     *  分片上传文件夹
     *
     * @param file 文件
     * @param fileName 文件名
     * @param fileSize 文件总大小
     * @param chunkIndex 分片标记数（1-9..）
     * @param totalChunks 分片总数
     * @param ownership 文件归属1:公共，2:用户，3：部门
     * @param parentId 父级文件夹id ，根目录为0
     * @param icon 图标
     * @param deptId 部门id，doc_dept_file_t表或者当前用户部门id
     * @return R:DocFileEntity
     * @throws IOException IO异常信息
     */
    @PostMapping(value = "/folderUpload/{ownership}/{parentId}")
    public R uploadFile(@RequestPart("file") MultipartFile file,
                         @RequestParam(value = "fileName") String fileName,
                         @RequestParam(value = "fileSize") long fileSize,
                         @RequestParam(value = "chunkIndex") int chunkIndex,
                         @RequestParam(value = "totalChunks") int totalChunks,
                         @PathVariable("ownership") Integer ownership,
                         @PathVariable("parentId") Long parentId,
                         @RequestParam(value = "icon", required = false) String icon,
                         @RequestParam(value = "deptId", required = false) Long deptId) {
        checkOwnership(ownership);
        DocFileEntity folder = checkDocFolder(parentId);
        checkShardFile(fileName);
        Integer editFlag = null;
        if (Objects.nonNull(folder)) {
            editFlag = folder.getEditFlag();
        }
        checkUserRolePermission(editFlag, ownership);
        DocFileDto fileDto = getDocFileDto(ownership, parentId, icon, deptId);
        fileDto.setFileName(fileName);
        fileDto.setFileSize(fileSize);

        return R.ok(uploadService.folderUpload(file, chunkIndex, totalChunks, fileDto), "分片上传文件夹成功！");
    }
}
