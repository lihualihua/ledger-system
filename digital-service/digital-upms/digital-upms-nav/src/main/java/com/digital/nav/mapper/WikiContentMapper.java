package com.digital.nav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digital.model.nav.entity.WikiContentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档收藏
 */
@Mapper
public interface WikiContentMapper extends BaseMapper<WikiContentEntity> {
}
