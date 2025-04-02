package com.digital.doc.controller;

import com.digital.common.core.constant.CacheConstants;
import com.digital.common.core.exception.ApplicationException;
import com.digital.common.core.exception.CheckedException;
import com.digital.common.core.util.R;
import com.digital.common.core.util.RedisUtils;
import com.digital.doc.service.IDownloadService;
import com.digital.model.doc.contains.DocContains;
import com.digital.model.doc.dto.DocFileDto;
import com.digital.model.doc.entity.DocFileEntity;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/doc/file")
@Tag(description = "doc", name = "文档下载")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class DownloadController extends BaseController {
    private static final Logger LOGGER = LogManager.getLogger(DownloadController.class);

    @Autowired
    private IDownloadService downloadService;

    /**
     * 单个文件下载，直接下载
     *
     * @param fileId
     * @param response
     */
    @GetMapping(value = "/download/{fileId}")
    public void download(@PathVariable("fileId") Long fileId, HttpServletResponse response) {
        DocFileEntity docFileEntity = checkDownloadFile(fileId);
        downloadService.download(docFileEntity, response);
        docFileService.addDownloadCount(docFileEntity);
    }

    private DocFileEntity checkDownloadFile(Long fileId) {
        DocFileEntity docFileEntity = RedisUtils.get(CacheConstants.FILE_DETAILS + CacheConstants.DELIMITER + fileId);
        if (Objects.isNull(docFileEntity)) {
            docFileEntity = docQueryService.findByIdNoUser(fileId);
            if (Objects.isNull(docFileEntity)) {
                throw new CheckedException("该文件不存在!");
            }
        }
        // 下载的对象不能是文件夹
        if (Objects.equals(docFileEntity.getFolderFlag(), DocContains.FOLDER_FLAG)) {
            LOGGER.error("下载的对象不能是文件夹!");
            throw new ApplicationException("服务器数据异常，请联系管理员!");
        }
        if (!new File(docFileEntity.getFilePath()).exists()) {
            throw new ApplicationException("文件不存在，请联系管理员!");
        }
        return docFileEntity;
    }

    /**
     * 分段下载
     *
     * @param fileId
     * @param request
     * @param response
     */
    @GetMapping(value = "/segment/download/{fileId}")
    public void segmentDownload(@PathVariable("fileId") Long fileId, HttpServletRequest request, HttpServletResponse response) {
        DocFileEntity docFileEntity = checkDownloadFile(fileId);

        // 完整下载
        String range = request.getHeader("Range");
        if (StringUtils.isBlank(range) && !"HEAD".equalsIgnoreCase(request.getMethod())) {
            downloadService.download(docFileEntity, response);
            docFileService.addDownloadCount(docFileEntity);
            return;
        }

        // 分段下载
        downloadService.segmentDownload(docFileEntity, request, response);

        // 记录下载次数
        if ("HEAD".equalsIgnoreCase(request.getMethod())) {
            docFileService.addDownloadCount(docFileEntity);
        }
    }

    /**
     * zip打包下载
     *
     * @param fileDtos
     * @param response
     */
    @PostMapping(value = "/batch/download")
    public void downloadZip(@RequestBody List<DocFileDto> fileDtos, HttpServletResponse response) {
        if (CollectionUtils.isEmpty(fileDtos)) {
            throw new CheckedException("未选择下载的文件!");
        }
        List<Long> fileIds = fileDtos.stream().map(DocFileDto::getFileId).collect(Collectors.toList());
        downloadService.downloadZip(fileIds, response);
    }

    /**
     * 打包，只打包不下载
     *
     * @param fileDtos
     */
    @PostMapping(value = "/zip")
    public R zip(@RequestBody List<DocFileDto> fileDtos) {
        if (CollectionUtils.isEmpty(fileDtos)) {
            throw new CheckedException("未选择下载的文件!");
        }
        List<Long> fileIds = fileDtos.stream().map(DocFileDto::getFileId).collect(Collectors.toList());
        return R.ok(downloadService.zip(fileIds));
    }
}
