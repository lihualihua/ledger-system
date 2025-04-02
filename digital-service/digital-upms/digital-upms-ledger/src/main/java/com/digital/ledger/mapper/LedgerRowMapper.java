package com.digital.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digital.model.ledger.entity.LedgerRowEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 行
 */
@Mapper
public interface LedgerRowMapper extends BaseMapper<LedgerRowEntity> {
    List<LedgerRowEntity> findRowList(@Param("tempId") String tempId,@Param("columnId") Long columnId, @Param("assigner") String assigner);

    List<LedgerRowEntity> findRows(@Param("tempId") String tempId, @Param("isFixed") Integer isFixed, @Param("assigner") String assigner);
}
