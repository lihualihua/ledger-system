package com.digital.doc.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.digital.common.core.constant.CacheConstants;
import com.digital.common.core.exception.ApplicationException;
import com.digital.common.core.exception.CheckedException;
import com.digital.common.core.handler.IThreadPoolHandler;
import com.digital.common.core.util.RedisUtils;
import com.digital.doc.config.DocFileConfig;
import com.digital.doc.helper.UserHelper;
import com.digital.doc.service.IDocFileService;
import com.digital.doc.service.IDocQueryService;
import com.digital.doc.service.IDownloadService;
import com.digital.model.doc.contains.DocContains;
import com.digital.model.doc.entity.DocFileEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DownloadServiceImpl implements IDownloadService {
    private static final Logger LOGGER = LogManager.getLogger(DownloadServiceImpl.class);

    @Autowired
    private IDocQueryService docQueryService;

    @Autowired
    private IDocFileService fileService;

    @Autowired
    private DocFileConfig docFileConfig;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private IThreadPoolHandler threadPoolHandler;

    @Override
    public void download(DocFileEntity docFileEntity, HttpServletResponse response) {
        //通过response的输出流返回文件
        ServletOutputStream outputStream = null;
        try {
            // 文件名带空格转换为 %20
            String fileName = URLEncoder.encode(docFileEntity.getFileName(), "UTF-8").replaceAll(" ", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setContentType("application/octet-stream");
            outputStream = response.getOutputStream();
            FileUtil.writeToStream(docFileEntity.getFilePath(), outputStream);
            outputStream.flush();
        } catch (IOException ex) {
            LOGGER.error(ex);
            throw new ApplicationException("文件下载异常，请联系管理员！");
        } finally {
            closeOs(outputStream);
        }
    }

    @Override
    public void segmentDownload(DocFileEntity docFileEntity, HttpServletRequest request, HttpServletResponse response) {
        InputStream is = null;
        OutputStream os = null;
        try {
            // chunk download Range  bytes=100-1000  100-
            long fileSize = docFileEntity.getFileSize();
            long pos = 0, last = fileSize - 1, sum = 0;
            String range = request.getHeader("Range");
            if (StringUtils.isNotBlank(range)) {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                String numberRange = range.replaceAll("bytes=", "");
                String[] strRange = numberRange.split("-");
                if (strRange.length == 2) {
                    pos = Long.parseLong(strRange[0].trim());
                    last = Long.parseLong(strRange[1].trim());
                    if (last > fileSize - 1) {
                        last = fileSize - 1;
                    }
                } else {
                    pos = Long.parseLong(numberRange.replaceAll("-", "").trim());
                }
            }
            long rangeLength = last - pos + 1;
            String contentRange = "bytes" + pos + "-" + last + "/" + fileSize;

            // 设置响应头
            String fileName = URLEncoder.encode(docFileEntity.getFileName(), "UTF-8");
            setSegmentDownloadHeader(fileName, contentRange, rangeLength, response);
            // 如果是head请求，则不做处理
            if ("HEAD".equalsIgnoreCase(request.getMethod()))  {
                return;
            }

            os = new BufferedOutputStream(response.getOutputStream());
            is = new BufferedInputStream(Files.newInputStream(Paths.get(docFileEntity.getFilePath())));
            is.skip(pos);

            byte[] buffer = new byte[getBufferSize(fileSize)];
            int length = 0;
            while (sum < rangeLength) {
                length = is.read(buffer, 0, Math.min((int) (rangeLength - sum), buffer.length));
                sum += length;
                os.write(buffer,0, length);
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
            throw new ApplicationException("文件下载异常，请联系管理员！");
        } finally {
            closeSteam(is, os);
        }
    }

    private int getBufferSize(long fileSize) {
        if (fileSize > DocContains.LARGE_FILE_SIZE) {
            return DocContains.LARGE_FILE_BUFFER_SIZE;
        }
        return DocContains.BUFFER_SIZE;
    }

    private static void closeSteam(InputStream is, OutputStream os) {
        if (os != null) {
            try {
                os.flush();
                os.close();
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
        }

        if (is != null) {
            try {
                is.close();
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
        }
    }

    private void setSegmentDownloadHeader(String fileName, String contentRange, long rangeLength, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/x-download");
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
        // Accept-Range
        response.setHeader("Accept-Range", "bytes");
        response.setHeader("Content-Range", contentRange);
        response.setHeader("Content-Length", String.valueOf(rangeLength));
    }

    @Override
    public void downloadZip(List<Long> fileIds, HttpServletResponse response) {
        List<DocFileEntity> docFileEntityList = getZipFile(fileIds);

        // zip打包
        DocFileEntity docFileEntity = zipFiles(fileIds.size(), docFileEntityList);
        download(docFileEntity, response);
        fileService.batchAddDownloadCount(docFileEntityList);
    }

    @Override
    public Map<String, String> zip(List<Long> fileIds) {
        List<DocFileEntity> docFileEntityList = getZipFile(fileIds);
        DocFileEntity docFileEntity = zipFiles(fileIds.size(), docFileEntityList);
        long snowflakeNextId = IdUtil.getSnowflakeNextId();
        RedisUtils.set(CacheConstants.FILE_DETAILS + CacheConstants.DELIMITER + snowflakeNextId, docFileEntity, 5, TimeUnit.MINUTES);
        Map<String, String> map = new HashMap<>();
        map.put("fileId", String.valueOf(snowflakeNextId));
        return map;
    }

    private List<DocFileEntity> getZipFile(List<Long> fileIds) {
        List<DocFileEntity> docFileEntityList = docQueryService.findAllChildFiles(fileIds, true, false, false);
        if (CollectionUtils.isEmpty(docFileEntityList)) {
            throw new CheckedException("您无权限下载这些文件！");
        }

        // 校验文件大小及数量
        checkFileCount(docFileEntityList);
        checkFileSize(docFileEntityList);
        return docFileEntityList;
    }

    private void closeOs(ServletOutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                LOGGER.error(e);
                throw new ApplicationException("文件流关闭异常，请联系管理员！");
            }
        }
    }

    private void checkFileCount(List<DocFileEntity> docFileEntityList) {
        int fileCount = (int) docFileEntityList.stream().filter(fileEntity -> fileEntity.getFolderFlag() == 0).count();
        int count = docFileConfig.getDownloadZipFileCount();
        if (fileCount > count) {
            throw new CheckedException("文件数量已超出最大限额【" + count + "】，请分批次下载!");
        }
    }

    private void checkFileSize(List<DocFileEntity> docFileEntityList) {
        long allFileSize = docFileEntityList.stream().filter(fileEntity -> fileEntity.getFolderFlag() == 0).mapToLong(DocFileEntity::getFileSize).sum();
        long maxFileSize = docFileConfig.getDownloadMaxFileSize();
        if (allFileSize > maxFileSize) {
            throw new CheckedException("文件大小已超出最大限额【" + FileUtil.readableFileSize(maxFileSize) + "】，请分批次下载!");
        }
    }

    private DocFileEntity zipFiles(int fileCount, List<DocFileEntity> docFileEntityList) {
        String userTmpPath = String.join("/", docFileConfig.getTmpPath(), userHelper.getUser().getUsername());
        String currentRootPath = String.join("/", userTmpPath, IdUtil.fastSimpleUUID(), "");
        File rootFile = new File(currentRootPath);
        if (!rootFile.exists()) {
            FileUtil.mkdir(rootFile);
        }

        // 复制文件
//        long copyStartTime = System.currentTimeMillis();
////        copyFile(docFileEntityList, currentRootPath);
//        copyFile2Tmp(docFileEntityList, currentRootPath);
//        LOGGER.info("Total time required to copy files ：{} ms", System.currentTimeMillis() - copyStartTime );

        // 生成zip文件信息
        String zipName = docFileEntityList.get(0).getFileName() + "等" + fileCount + "个文件.zip";
        String zipUUIDName = IdUtil.fastSimpleUUID() + ".zip";
        String zipPath = userTmpPath + "/" + zipUUIDName;
        File zipFile = new File(zipPath);
        if (zipFile.exists()) {
            FileUtil.del(zipFile);
        }
        long zipStartTime = System.currentTimeMillis();
//        ZipUtil.zip(zipFile, false, new File(currentRootPath));
        compressFiles2Zip(docFileEntityList, zipFile.getPath());
        LOGGER.info("Total time required to package the zip file：{} ms", System.currentTimeMillis() - zipStartTime);

        DocFileEntity docFileEntity = new DocFileEntity();
        docFileEntity.setFileName(zipName);
        docFileEntity.setFilePath(zipPath);
        docFileEntity.setFolderFlag(0);
        docFileEntity.setFileSize(zipFile.length());
        return docFileEntity;
    }

    private void compressFiles2Zip(List<DocFileEntity> docFileEntityList, String zipFilePath) {
        Map<Long, String> filePathMap = new HashMap<>();
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            // 创建zip中的文件夹
            docFileEntityList.stream().filter(item -> item.getFolderFlag() == 1).forEach(docFileEntity -> {
                    String parentFilePath = filePathMap.get(docFileEntity.getParentId());
                    String folderName = null;
                    if (StringUtils.isBlank(parentFilePath)) {
                        folderName = docFileEntity.getFileName().concat("/");
                    } else {
                        folderName = parentFilePath + docFileEntity.getFileName() + "/";
                    }
                    filePathMap.put(docFileEntity.getFileId(), folderName);
            });
            // 获取文件并去重
            Map<Long, DocFileEntity> fileEntityMap = docFileEntityList.stream().filter(item ->
                    item.getFolderFlag() == 0).collect(Collectors.toMap(DocFileEntity::getFileId, item -> item, (item, item1) -> item));
            CountDownLatch latch = new CountDownLatch(fileEntityMap.size());
            fileEntityMap.values().forEach(docFileEntity -> {
                Future<?> future = threadPoolHandler.submit(() -> {
                    try (FileInputStream fis = new FileInputStream(docFileEntity.getFilePath())) {
                        String parentFilePath = filePathMap.get(docFileEntity.getParentId());
                        String filePath = null;
                        if (StringUtils.isBlank(parentFilePath)) {
                            filePath = docFileEntity.getFileName();
                        } else {
                            filePath = parentFilePath + docFileEntity.getFileName();
                        }
                        ZipEntry fileEntry = new ZipEntry(filePath);
                        // 将文件条目添加到ZIP输出流
                        zos.putNextEntry(fileEntry);
                        int len;
                        byte[] buffer = new byte[DocContains.BUFFER_SIZE];
                        // 读取fis字节流，转移到buffer字节数组中去，读取后fis为空
                        while ((len = fis.read(buffer)) > 0) {
                            zos.write(buffer,0, len);
                        }
                    } catch (IOException ex) {
                        LOGGER.error(ex);
                        throw new ApplicationException(ex);
                    } finally {
                        latch.countDown();
                        try {
                            zos.closeEntry();
                            zos.flush();
                        } catch (IOException e) {
                            LOGGER.error("zip输出流关闭条目失败！");
                            LOGGER.error(e);
                        }
                    }
                });
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error(e);
                    throw new ApplicationException("服务器内部异常，zip打包失败!");
                }
            });
            latch.await();
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ApplicationException("服务器内部异常，zip打包失败!");
        }
    }

    private void copyFile(List<DocFileEntity> docFileEntityList, String currentRootPath) {
        Map<Long, String> filesMap = new HashMap<>();
        docFileEntityList.forEach(docFileEntity -> {
            try {
                // 如果是文件夹，则暂存id和路径
                String descFilePath = filesMap.get(docFileEntity.getParentId());
                if (StringUtils.isBlank(descFilePath)) {
                    descFilePath = currentRootPath;
                }
                if (docFileEntity.getFolderFlag() == 1) {
                    File file = new File(descFilePath + docFileEntity.getFileName());
                    file.mkdirs();
                    filesMap.put(docFileEntity.getFileId(), file.getPath() + "/");
                } else {
                    File descFile = new File(descFilePath + docFileEntity.getFileName());
                    File currentFile = new File(docFileEntity.getFilePath());
                    FileUtils.copyFile(currentFile, descFile);
                }
            } catch (IOException e) {
                LOGGER.error(e);
                throw new ApplicationException("服务器内部异常，请联系管理员!");
            }
        });
    }

    private void copyFile2Tmp(List<DocFileEntity> docFileEntityList, String currentRootPath) {
        Map<Long, String> filesMap = new HashMap<>();
        // 先创建文件夹
        docFileEntityList.stream().filter(item -> item.getFolderFlag() == 1).forEach(docFileEntity -> {
            String targetFilePath = getDescFilePath(filesMap, docFileEntity, currentRootPath);
            File file = new File(targetFilePath + docFileEntity.getFileName());
            file.mkdirs();
            filesMap.put(docFileEntity.getFileId(), file.getPath() + "/");
        });

        // 并发拷贝文件
        List<DocFileEntity> files = docFileEntityList.stream().filter(item -> item.getFolderFlag() == 0).collect(Collectors.toList());
        LOGGER.info("The number of files to be downloaded ：{}", files.size());
        CountDownLatch latch = new CountDownLatch(files.size());
        files.forEach(docFileEntity -> threadPoolHandler.execute(() -> {
            try {
                String targetFilePath = getDescFilePath(filesMap, docFileEntity, currentRootPath);
                File descFile = new File(targetFilePath + docFileEntity.getFileName());
                File currentFile = new File(docFileEntity.getFilePath());
                FileUtils.copyFile(currentFile, descFile);
                latch.countDown();
            } catch (IOException e) {
                LOGGER.error("多线程拷贝文件异常!");
                LOGGER.error(e);
                throw new ApplicationException("服务器内部异常，请联系管理员!");
            }
        }));
        try {
            latch.await();
        } catch (InterruptedException e) {
            LOGGER.error(e);
            throw new ApplicationException("服务器内部异常，请联系管理员!");
        }
    }

    private String getDescFilePath(Map<Long, String> filesMap, DocFileEntity docFileEntity, String currentRootPath) {
        String targetFilePath = filesMap.get(docFileEntity.getParentId());
        if (StringUtils.isBlank(targetFilePath)) {
            targetFilePath = currentRootPath;
        }
        return targetFilePath;
    }
}
