package com.digital.doc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digital.model.doc.entity.DocFunctionEntity;
import com.digital.model.doc.entity.DocUserFunctionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DocUserFunctionMapper extends BaseMapper<DocUserFunctionEntity> {
    List<DocFunctionEntity> findMyFunction(@Param("userId") Long userId);
}
