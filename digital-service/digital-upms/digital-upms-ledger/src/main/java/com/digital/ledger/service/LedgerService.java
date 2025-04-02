package com.digital.ledger.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.digital.model.ledger.dto.LedgerDataDto;
import com.digital.model.ledger.entity.LedgerEntity;
import com.digital.model.ledger.response.LedgerDataVO;

import java.util.List;

public interface LedgerService extends IService<LedgerEntity> {

    LedgerEntity findByTempId(String tempId);

    IPage<LedgerEntity> findMyLedgerList(PageDTO page);

    IPage<LedgerEntity> findLedgerListWithMe(PageDTO page);

    LedgerDataVO findLedgerData(String tempId);

    void saveLedgerData(List<LedgerDataDto> dataDtoList, LedgerEntity ledgerEntity);
}
