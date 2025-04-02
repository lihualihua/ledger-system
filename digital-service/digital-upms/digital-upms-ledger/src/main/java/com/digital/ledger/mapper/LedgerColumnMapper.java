package com.digital.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digital.model.ledger.entity.LedgerColumnEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 列
 */
@Mapper
public interface LedgerColumnMapper extends BaseMapper<LedgerColumnEntity> {

    List<LedgerColumnEntity> findColumnList(@Param("tempId") String tempId, @Param("assigner") String assigner);
}
