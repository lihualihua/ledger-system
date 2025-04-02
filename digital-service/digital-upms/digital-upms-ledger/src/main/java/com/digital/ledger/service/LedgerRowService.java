package com.digital.ledger.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digital.model.ledger.entity.LedgerRowEntity;

import java.util.List;

public interface LedgerRowService extends IService<LedgerRowEntity> {
    List<LedgerRowEntity> findRowList(String tempId, Long columnId, String assigner);

    List<LedgerRowEntity> findAllRows(String tempId, Integer isFixed);

    List<LedgerRowEntity> findRowsByUser(String tempId, Integer isFixed);

    Long findAssignerCellCount(String tempId, String username, boolean isEmpty);
}
