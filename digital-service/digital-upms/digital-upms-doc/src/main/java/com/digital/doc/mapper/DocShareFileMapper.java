package com.digital.doc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digital.model.doc.entity.DocFileEntity;
import com.digital.model.doc.entity.DocShareEntity;
import com.digital.model.doc.entity.DocShareFileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档收藏
 */
@Mapper
public interface DocShareFileMapper extends BaseMapper<DocShareFileEntity> {

    List<DocFileEntity> findFilesByMd5Value(@Param("shareEntity") DocShareEntity shareEntity);
}
