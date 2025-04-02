package com.digital.doc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digital.model.doc.entity.DocHistoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 搜索历史内容
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
@Mapper
public interface DocHistoryMapper extends BaseMapper<DocHistoryEntity> {

    Page<DocHistoryEntity> findHistoryList(Page<DocHistoryEntity> page, @Param("fileEntity") DocHistoryEntity fileEntity, @Param("userId") Long userId);

    int findHistoryByContentAndUserId(@Param("fileEntity") DocHistoryEntity fileEntity, @Param("userId") Long userId);
}
