package com.digital.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.digital.model.ledger.entity.LedgerEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 模版
 */
@Mapper
public interface LedgerMapper extends BaseMapper<LedgerEntity> {

    IPage<LedgerEntity> findLedgerListWithMe(PageDTO page, @Param("assigner") String assigner);
}
