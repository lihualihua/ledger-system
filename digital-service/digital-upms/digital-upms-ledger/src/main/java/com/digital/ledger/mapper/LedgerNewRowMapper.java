package com.digital.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digital.model.ledger.entity.LedgerNewRowEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 行
 */
@Mapper
public interface LedgerNewRowMapper extends BaseMapper<LedgerNewRowEntity> {
}
