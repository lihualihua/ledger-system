package com.digital.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.doc.helper.UserHelper;
import com.digital.ledger.enums.LedgerTypeEnum;
import com.digital.ledger.mapper.LedgerRowMapper;
import com.digital.ledger.service.LedgerRowService;
import com.digital.ledger.service.LedgerService;
import com.digital.model.ledger.entity.LedgerRowEntity;
import com.digital.model.ledger.entity.LedgerEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class LedgerRowServiceImpl extends ServiceImpl<LedgerRowMapper, LedgerRowEntity> implements LedgerRowService {

    @Autowired
    private LedgerRowMapper ledgerRowMapper;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private UserHelper userHelper;

    @Override
    public List<LedgerRowEntity> findRowList(String tempId, Long columnId, String assigner) {
        return ledgerRowMapper.findRowList(tempId, columnId,assigner);
    }

    @Override
    public List<LedgerRowEntity> findAllRows(String tempId, Integer isFixed) {
        LedgerEntity templateEntity = ledgerService.findByTempId(tempId);
        if (Objects.isNull(templateEntity)) {
            return Collections.emptyList();
        }
        // 只有创建人才能查看所有数据
        String username = userHelper.getUser().getUsername();
        if (!Objects.equals(username, templateEntity.getCreateBy())) {
            return Collections.emptyList();
        }
        return baseMapper.findRows(tempId, isFixed, null);
    }

    @Override
    public List<LedgerRowEntity> findRowsByUser(String tempId, Integer isFixed) {
        LedgerEntity templateEntity = ledgerService.findByTempId(tempId);
        if (Objects.isNull(templateEntity)) {
            return Collections.emptyList();
        }

        String username = userHelper.getUser().getUsername();
        List<LedgerRowEntity> userRows = baseMapper.findRows(tempId, isFixed, username);
        if (CollectionUtils.isNotEmpty(userRows) && Objects.equals(templateEntity.getType(), LedgerTypeEnum.CELL_TEMPLATE.getType())) {
            List<Long> rowIds = userRows.stream().map(LedgerRowEntity::getRowId).collect(Collectors.toList());
            userRows = baseMapper.selectList(Wrappers.<LedgerRowEntity>lambdaQuery().eq(LedgerRowEntity::getTempId, tempId).in(
                    LedgerRowEntity::getRowId, rowIds));
        }
        return userRows;
    }

    @Override
    public Long findAssignerCellCount(String tempId, String username, boolean isEmpty) {
        LedgerEntity templateEntity = ledgerService.findByTempId(tempId);
        if (Objects.isNull(templateEntity)) {
            return 0L;
        }

        LambdaQueryWrapper<LedgerRowEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (isEmpty) {
            queryWrapper.isNull(LedgerRowEntity::getContent);
        } else {
            queryWrapper.isNotNull(LedgerRowEntity::getContent);
        }
        queryWrapper.eq(LedgerRowEntity::getTempId, tempId);
        queryWrapper.eq(LedgerRowEntity::getAssigner, username);
        queryWrapper.eq(LedgerRowEntity::getIsEdit, 1);
        return baseMapper.selectCount(queryWrapper);
    }
}
