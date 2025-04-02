package com.digital.doc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.digital.model.doc.entity.DocFileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件管理
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
@Mapper
public interface DocFileMapper extends BaseMapper<DocFileEntity> {

    int addCollectCount(Long fileId);

    int subtractCollectCount(Long fileId);

    int addDownloadCount(Long fileId);

    int batchAddDownloadCount(List<Long> fileIds);

    int addPreviewCount(Long fileId);

    IPage<DocFileEntity> findChildFiles(PageDTO<DocFileEntity> page, @Param("fileEntity") DocFileEntity fileEntity, @Param("userId") Long userId, @Param("deptId") Long deptId);

    IPage<DocFileEntity> findAllChildFiles(@Param("page") PageDTO<DocFileEntity> page, @Param("fileEntity") DocFileEntity fileEntity, @Param("userId") Long userId, @Param("deptId") Long deptId);

    IPage<DocFileEntity> findCollectFiles(PageDTO<DocFileEntity> page, @Param("fileEntity") DocFileEntity fileEntity, @Param("userId") Long userId);

    DocFileEntity findFileById(@Param("fileEntity") DocFileEntity fileEntity, @Param("userId") Long userId, @Param("deptId") Long deptId);

    List<DocFileEntity> findFileByParentIds(@Param("fileIds") List<Long> fileIds, @Param("userId") Long userId, @Param("deptId") Long deptId, @Param("containDel") boolean containDel);

    DocFileEntity findDocFileEntityByUserId(@Param("fileEntity") DocFileEntity fileEntity, @Param("userId") Long userId);

    DocFileEntity findDocFileEntityByDeptId(@Param("fileEntity") DocFileEntity fileEntity, @Param("deptId") Long deptId);

    Long findUserDeptFileCount(@Param("fileEntity") DocFileEntity fileEntity, @Param("userId") Long userId, @Param("deptId") Long deptId);

    List<DocFileEntity> findAllParentFiles(@Param("fileEntity") DocFileEntity fileEntity);
}

