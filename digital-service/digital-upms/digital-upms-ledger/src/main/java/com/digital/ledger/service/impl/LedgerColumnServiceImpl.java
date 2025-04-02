package com.digital.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.doc.helper.UserHelper;
import com.digital.ledger.mapper.LedgerColumnMapper;
import com.digital.ledger.service.LedgerColumnService;
import com.digital.ledger.service.LedgerService;
import com.digital.model.ledger.entity.LedgerColumnEntity;
import com.digital.model.ledger.entity.LedgerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = Exception.class)
public class LedgerColumnServiceImpl extends ServiceImpl<LedgerColumnMapper, LedgerColumnEntity> implements LedgerColumnService {

    @Autowired
    private LedgerService templateService;

    @Autowired
    private UserHelper userHelper;

    @Override
    public List<LedgerColumnEntity> findLedgerColumnList(LedgerColumnEntity ledgerColumn) {
        String tempId = ledgerColumn.getTempId();
        LedgerEntity templateEntity = templateService.findByTempId(tempId);
        if (Objects.isNull(templateEntity)) {
            return Collections.emptyList();
        }
//        if (Objects.equals(templateEntity.getCreateById(), userHelper.getUserId())
//                || Objects.equals(templateEntity.getType(), TemplateTypeEnum.ROW_TEMPLATE.getType())) {
            LambdaQueryWrapper<LedgerColumnEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LedgerColumnEntity::getTempId, ledgerColumn.getTempId());
            queryWrapper.eq(Objects.nonNull(ledgerColumn.getIsFixed()) ,LedgerColumnEntity::getIsFixed, ledgerColumn.getIsFixed());
            queryWrapper.orderByAsc(LedgerColumnEntity::getSort);
            return baseMapper.selectList(queryWrapper);
//        }
//        return baseMapper.findColumnList(tempId, userHelper.getUser().getUsername());
    }
}
