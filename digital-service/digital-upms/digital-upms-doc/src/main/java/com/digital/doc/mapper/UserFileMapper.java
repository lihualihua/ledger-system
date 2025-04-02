package com.digital.doc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digital.model.doc.entity.UserFileEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档收藏
 */
@Mapper
public interface UserFileMapper extends BaseMapper<UserFileEntity> {

}
