package com.digital.doc.controller;

import com.digital.common.core.exception.CheckedException;
import com.digital.doc.config.DocFileConfig;
import com.digital.doc.enums.OwnershipEnum;
import com.digital.doc.helper.UserPermissionHelper;
import com.digital.doc.service.IDocFileService;
import com.digital.doc.service.IDocQueryService;
import com.digital.model.doc.contains.DocContains;
import com.digital.model.doc.dto.DocFileDto;
import com.digital.model.doc.entity.DocFileEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Objects;

public abstract class BaseController {
    private static final Logger LOGGER = LogManager.getLogger(BaseController.class);

    @Autowired
    protected IDocFileService docFileService;

    @Autowired
    protected IDocQueryService docQueryService;

    @Autowired
    protected DocFileConfig docFileConfig;

    @Autowired
    protected UserPermissionHelper userPermissionHelper;

    protected DocFileEntity checkDocFolder(Long folderId) throws CheckedException {
        if (Objects.equals(folderId, DocContains.ROOT_PARENT_ID)) {
            return null;
        }
        DocFileEntity docFileEntity = findById(folderId);
        if (Objects.isNull(docFileEntity)) {
            throw new CheckedException("父级文件夹不存在!");
        }
        return docFileEntity;
    }

    protected DocFileEntity checkDocFile(Long fileId) {
        DocFileEntity docFileEntity = findById(fileId);
        if (Objects.isNull(docFileEntity)) {
            throw new CheckedException("文件或文件夹不存在!");
        }
        return docFileEntity;
    }

    protected DocFileEntity findById(Long fileId) throws CheckedException {
        return docQueryService.findDelById(fileId, false);
    }

    protected void checkOwnership(Integer ownership) {
        if (!OwnershipEnum.getOwnershipValues().contains(ownership)) {
            LOGGER.error("文件归属：{}", ownership);
            throw new CheckedException("文件归属不正确!");
        }
    }

    protected void checkFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            throw new CheckedException("文件名称不能为空!");
        }

        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!docFileConfig.getAllFileTypes().contains(fileType.toLowerCase(Locale.ROOT))) {
            LOGGER.error("该文件类型不支持! 文件类型：{}", fileType);
            throw new CheckedException("该文件类型不支持!");
        }


        // TODO 单个文件暂不校验，通过spring multi文件校验
/*        long fileSize = file.getSize();
        long maxFileSize = fileHelper.getUploadMaxFileSize();
        if (fileSize > maxFileSize) {
            throw new MaxUploadSizeExceededException(maxFileSize);
//        }*/
    }

    /**
     * 分片上传文件名检查
     * @param fileName 文件名称
     */
    protected void checkShardFile(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new CheckedException("文件名称不能为空!");
        }

        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!docFileConfig.getAllFileTypes().contains(fileType.toLowerCase(Locale.ROOT))) {
            LOGGER.error("文件类型：{}", fileType);
            throw new CheckedException("该文件类型不支持!");
        }
    }

    /**
     * 公共文档仅允许超级管理员上传、创建文件夹
     *
     * @param ownership
     */
    protected void checkUserRolePermission(Integer editFlag, Integer ownership) {
        userPermissionHelper.checkUserRolePermission(editFlag, ownership);
    }

    /**
     * 检查文件夹是否存在
     *
     * @param docFileDto
     */
    protected void checkFolderExist(DocFileDto docFileDto) {
        docFileDto.setDelFlag(1);
        DocFileEntity entity = docQueryService.findDocFileEntity(docFileDto);
        if (Objects.nonNull(entity)) {
            LOGGER.error("文件夹名称：{}", entity.getFileName());
            throw new CheckedException("文件夹已存在，请勿重复创建！");
        }
    }
}
