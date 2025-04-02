package com.digital.ledger.helper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.digital.doc.helper.UserHelper;
import com.digital.ledger.mapper.LedgerAssignerMapper;
import com.digital.ledger.service.LedgerRowService;
import com.digital.model.ledger.entity.LedgerAssignerEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LedgerAssignerHelper {

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private LedgerAssignerMapper assignerMapper;

    @Autowired
    private LedgerRowService ledgerRowService;

    /**
     * 插入台账用户单元格数量
     *
     * @param tempId
     * @param tempType
     * @param cellValueList
     * @param columnSize
     */
    public void insertLedgerAssigners(String tempId, int tempType, List<String> cellValueList, int columnSize) {
        if (CollectionUtils.isEmpty(cellValueList)) {
            return;
        }
        if (tempType == 1) {
            Map<String, List<String>> usernameMap = cellValueList.stream().collect(Collectors.groupingBy(s -> s));
            usernameMap.keySet().forEach(str -> insertLedgerAssignerEntity(tempId, str, usernameMap.get(str).size() * columnSize));
        } else {
            cellValueList = cellValueList.stream().distinct().collect(Collectors.toList());
            cellValueList.forEach(str -> {
                long total = ledgerRowService.findAssignerCellCount(tempId, str, true);
                insertLedgerAssignerEntity(tempId, str, (int) total);
            });
        }
    }

    private void insertLedgerAssignerEntity(String tempId, String str, int total) {
        LedgerAssignerEntity assignerEntity = new LedgerAssignerEntity();
        assignerEntity.setTempId(tempId);
        assignerEntity.setAssigner(str);
        assignerEntity.setCurrent(0);
        assignerEntity.setTotal(total);
        assignerEntity.setStatus(1);
        assignerMapper.insert(assignerEntity);
    }

    /**
     * 修改台账单元格数量
     *
     * @param tempId
     */
    public void updateLedgerAssigners(String tempId) {
        String username = userHelper.getUser().getUsername();
        long current = ledgerRowService.findAssignerCellCount(tempId, username, false);
        LambdaUpdateWrapper<LedgerAssignerEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(LedgerAssignerEntity::getCurrent, current);
        updateWrapper.eq(LedgerAssignerEntity::getTempId, tempId);
        updateWrapper.eq(LedgerAssignerEntity::getAssigner, username);
        updateWrapper.set(LedgerAssignerEntity::getUpdateBy, userHelper.getUser().getName());
        updateWrapper.set(LedgerAssignerEntity::getUpdateTime, LocalDateTime.now());
        updateWrapper.set(LedgerAssignerEntity::getStatus, 0);
        assignerMapper.update(updateWrapper);
    }

    /**
     * 查找台账分配人
     *
     * @param tempId
     * @return
     */
    public LedgerAssignerEntity findAssignerEntity(String tempId) {
        String username = userHelper.getUser().getUsername();
        return assignerMapper.selectOne(Wrappers.<LedgerAssignerEntity>lambdaQuery().eq(LedgerAssignerEntity::getTempId, tempId).eq(LedgerAssignerEntity::getAssigner, username));
    }
}
