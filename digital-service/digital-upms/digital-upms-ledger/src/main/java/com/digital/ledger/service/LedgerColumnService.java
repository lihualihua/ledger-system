package com.digital.ledger.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digital.model.ledger.entity.LedgerColumnEntity;

import java.util.List;

public interface LedgerColumnService extends IService<LedgerColumnEntity> {

    List<LedgerColumnEntity> findLedgerColumnList(LedgerColumnEntity ledgerColumn);
}
