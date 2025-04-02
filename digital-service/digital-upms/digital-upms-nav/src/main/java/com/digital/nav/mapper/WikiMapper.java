package com.digital.nav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.digital.model.nav.dto.WikiDto;
import com.digital.model.nav.entity.WikiEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 文档收藏
 */
@Mapper
public interface WikiMapper extends BaseMapper<WikiEntity> {
    IPage<WikiEntity> findWikiReleaseList(PageDTO page, @Param("wiki") WikiDto wiki);
}
