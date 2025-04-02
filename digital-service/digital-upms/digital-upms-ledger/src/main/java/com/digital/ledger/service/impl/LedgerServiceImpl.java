package com.digital.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.common.core.exception.ApplicationException;
import com.digital.doc.helper.UserHelper;
import com.digital.ledger.enums.LedgerStatusEnum;
import com.digital.ledger.enums.LedgerTypeEnum;
import com.digital.ledger.helper.LedgerAssignerHelper;
import com.digital.ledger.mapper.LedgerMapper;
import com.digital.ledger.service.LedgerColumnService;
import com.digital.ledger.service.LedgerRowService;
import com.digital.ledger.service.LedgerService;
import com.digital.model.ledger.dto.LedgerDataDto;
import com.digital.model.ledger.entity.LedgerColumnEntity;
import com.digital.model.ledger.entity.LedgerEntity;
import com.digital.model.ledger.entity.LedgerRowEntity;
import com.digital.model.ledger.response.LedgerCellVO;
import com.digital.model.ledger.response.LedgerColumnVO;
import com.digital.model.ledger.response.LedgerDataVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional(rollbackFor = Exception.class)
public class LedgerServiceImpl extends ServiceImpl<LedgerMapper, LedgerEntity> implements LedgerService {
    private static final Logger LOGGER = LogManager.getLogger(LedgerServiceImpl.class);

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private LedgerColumnService ledgerColumnService;

    @Autowired
    private LedgerRowService ledgerRowService;

    @Autowired
    private LedgerAssignerHelper assignerHelper;

    @Override
    public LedgerEntity findByTempId(String tempId) {
        return baseMapper.selectOne(Wrappers.<LedgerEntity>lambdaQuery().eq(LedgerEntity::getTempId, tempId));
    }

    @Override
    public IPage<LedgerEntity> findMyLedgerList(PageDTO page) {
        return baseMapper.selectPage(page, Wrappers.<LedgerEntity>lambdaQuery().eq(
                LedgerEntity::getCreateById, userHelper.getUserId()).orderByDesc(LedgerEntity::getCreateTime));
    }

    @Override
    public IPage<LedgerEntity> findLedgerListWithMe(PageDTO page) {
        return baseMapper.findLedgerListWithMe(page, userHelper.getUser().getUsername());
    }

    @Override
    public LedgerDataVO findLedgerData(String tempId) {
        LedgerDataVO templateData = new LedgerDataVO();
        LedgerEntity templateEntity = findByTempId(tempId);
        if (Objects.isNull(templateEntity) || Objects.equals(templateEntity.getStatus(), LedgerStatusEnum.ARCHIVED.getCode())) {
            return templateData;
        }

        LedgerColumnEntity columnEntity = new LedgerColumnEntity();
        columnEntity.setTempId(tempId);
        List<LedgerColumnEntity> columnList = ledgerColumnService.findLedgerColumnList(columnEntity);
        if (CollectionUtils.isEmpty(columnList)) {
            return templateData;
        }

        Map<Long, String> columnIdMap = setColumns(columnList, templateData);
        List<LedgerRowEntity> rowList = null;
        if (StringUtils.equals(userHelper.getUser().getUsername(), templateEntity.getCreateBy())) {
            rowList = ledgerRowService.findAllRows(tempId, null);
        } else {
            rowList = ledgerRowService.findRowsByUser(tempId, null);
        }
        if (CollectionUtils.isEmpty(rowList)) {
            return templateData;
        }
        setData(rowList, columnIdMap, templateData, templateEntity);
        return templateData;
    }

    @Override
    public void saveLedgerData(List<LedgerDataDto> dataDtoList, LedgerEntity ledgerEntity) {
        String username = userHelper.getUser().getName();
        Long userId = userHelper.getUserId();
        String tempId = ledgerEntity.getTempId();
        dataDtoList.forEach(item -> {
            Long id = item.getId();
            LedgerRowEntity rowEntity = null;
            if (Objects.isNull(id)) {
                rowEntity = ledgerRowService.getOne(Wrappers.<LedgerRowEntity>lambdaQuery().eq(
                        LedgerRowEntity::getRowId, item.getRowId()).eq(LedgerRowEntity::getColumnId, item.getColumnId())
                        .eq(LedgerRowEntity::getAssigner, username).eq(LedgerRowEntity::getTempId, tempId));
                if (Objects.isNull(rowEntity)) {
                    rowEntity = new LedgerRowEntity();
                    rowEntity.setContent(item.getValue());
                    rowEntity.setAssigner(username);
                    rowEntity.setColumnId(item.getColumnId());
                    rowEntity.setRowId(item.getRowId());
                    rowEntity.setTempId(tempId);
                    rowEntity.setIsView(0);
                    rowEntity.setIsEdit(1);
                    rowEntity.setType(ledgerEntity.getType());
                    rowEntity.setCreateById(userId);
                    ledgerRowService.save(rowEntity);
                }
            } else {
                rowEntity = ledgerRowService.getOne(Wrappers.<LedgerRowEntity>lambdaQuery().eq(
                        LedgerRowEntity::getId, id).eq(LedgerRowEntity::getAssigner, username).eq(LedgerRowEntity::getTempId, tempId));
                if (Objects.isNull(rowEntity)) {
                    throw new ApplicationException("台账数据异常，请联系管理员!");
                }
                LambdaUpdateWrapper<LedgerRowEntity> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(LedgerRowEntity::getContent, item.getValue());
                updateWrapper.eq(LedgerRowEntity::getId, item.getId());
                updateWrapper.set(LedgerRowEntity::getUpdateBy, username);
                updateWrapper.set(LedgerRowEntity::getUpdateTime, LocalDateTime.now());
                ledgerRowService.update(updateWrapper);
            }
        });
        assignerHelper.updateLedgerAssigners(tempId);
    }

    private Map<Long, String> setColumns(List<LedgerColumnEntity> columnList, LedgerDataVO templateData) {
        List<LedgerColumnVO> columns = new ArrayList<>();
        Map<Long, String> columnIdMap = new HashMap<>();
        IntStream.range(0, columnList.size()).forEach(index -> {
            String name = columnList.get(index).getName();
            // 如果列名为空，不加载数据
            if (StringUtils.isBlank(name)) {
                return;
            }
            Long columnId = columnList.get(index).getId();
            LedgerColumnVO.LedgerColumnAttr columnAttr = new LedgerColumnVO.LedgerColumnAttr();
            if (index == 0) {
                columnAttr.setWidth("130px");
                columnAttr.setFixed("left");
            } else {
                columnAttr.setWidth("");
            }
            columnIdMap.put(columnId, name);
            columns.add(new LedgerColumnVO.Builder().setLabel(name).setProp(name).setAttrs(columnAttr).build());
        });
        templateData.setColumns(columns);
        return columnIdMap;
    }

    private void setData(List<LedgerRowEntity> rowList, Map<Long, String> columnIdMap, LedgerDataVO templateData, LedgerEntity templateEntity) {
        Map<Long, List<LedgerRowEntity>> rowsMap = rowList.stream().collect(Collectors.groupingBy(LedgerRowEntity::getRowId));
        List<Map<String, LedgerCellVO>> data = new ArrayList<>();
        rowsMap.keySet().stream().sorted().forEach(rowId -> {
            List<LedgerRowEntity> rows = rowsMap.get(rowId);
            Map<String, LedgerCellVO> rowMap = new HashMap<>();
            Map<Long, LedgerRowEntity> rowColumnIdMap = rows.stream().collect(Collectors.toMap(LedgerRowEntity::getColumnId,
                    item -> item, (v1, v2) -> v1));
            columnIdMap.keySet().stream().sorted().forEach(item -> {
                LedgerRowEntity rowEntity = rowColumnIdMap.get(item);
                // 如果列名为空，不加载数据
                String columnName = columnIdMap.get(item);
                if (StringUtils.isBlank(columnName)) {
                    return;
                }
                if (Objects.isNull(rowEntity)) {
                    rowMap.put(columnName, buildCellVO(rowId, null, item, templateEntity));
                    return;
                }
                rowMap.put(columnName, buildCellVO(rowId, rowEntity, item, templateEntity));
            });
            data.add(rowMap);
        });
        templateData.setData(data);
    }

    private LedgerCellVO buildCellVO(Long rowId, LedgerRowEntity rowEntity, Long columnId, LedgerEntity templateEntity) {
        LedgerCellVO cellVO = new LedgerCellVO();
        if (Objects.nonNull(rowEntity)) {
            cellVO.setId(rowEntity.getId());
            cellVO.setEdit(rowEntity.getIsEdit() == 1 && userHelper.getUser().getName().equals(rowEntity.getAssigner()));
            String content = rowEntity.getContent();
            content = StringUtils.isBlank(content) ? "" : content;
            cellVO.setValue(content);
        } else {
            setCellEdit(templateEntity, cellVO, rowId);
            cellVO.setValue("");
        }
        cellVO.setColumnId(columnId);
        cellVO.setRowId(rowId);
        return cellVO;
    }

    private void setCellEdit(LedgerEntity templateEntity, LedgerCellVO cellVO, Long rowId) {
        if (Objects.equals(templateEntity.getType(), LedgerTypeEnum.ROW_TEMPLATE.getType())) {
            List<LedgerRowEntity> rowEntityList = ledgerRowService.list(Wrappers.<LedgerRowEntity>lambdaQuery().eq(
                            LedgerRowEntity::getRowId, rowId).eq(LedgerRowEntity::getAssigner,
                    userHelper.getUser().getName()).eq(LedgerRowEntity::getTempId, templateEntity.getTempId()));
            if (Objects.isNull(rowEntityList)) {
                cellVO.setEdit(false);
                return;
            }
            cellVO.setEdit(true);
            return;
        }
        cellVO.setEdit(false);
    }
}
