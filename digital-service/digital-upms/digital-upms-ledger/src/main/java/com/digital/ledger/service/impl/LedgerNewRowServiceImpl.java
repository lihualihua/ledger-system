package com.digital.ledger.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.ledger.mapper.LedgerNewRowMapper;
import com.digital.ledger.service.ILedgerNewRowService;
import com.digital.model.ledger.entity.LedgerNewRowEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class LedgerNewRowServiceImpl extends ServiceImpl<LedgerNewRowMapper, LedgerNewRowEntity> implements ILedgerNewRowService {

}
