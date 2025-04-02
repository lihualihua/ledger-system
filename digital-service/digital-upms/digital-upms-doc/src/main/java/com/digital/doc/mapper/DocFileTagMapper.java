package com.digital.doc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.digital.model.doc.dto.DocTagDto;
import com.digital.model.doc.entity.DocFileEntity;
import com.digital.model.doc.entity.DocFileTagEntity;
import com.digital.model.doc.entity.DocTagEntity;
import com.digital.model.doc.response.DocTagResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 文档收藏
 */
@Mapper
public interface DocFileTagMapper extends BaseMapper<DocFileTagEntity> {
    IPage<DocFileEntity> findFilesByTag(PageDTO page, @Param("tagEntity") DocTagEntity tagEntity, @Param("fileName") String fileName);

    Page<DocTagResponse> findPublicTags(Page page, @Param("tagEntity") DocTagDto docTagDto, @Param("deptId") Long deptId);

    List<DocTagEntity> findTagByFileId(@Param("fileId") Long fileId, @Param("userId") Long userId);
}
