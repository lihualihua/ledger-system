package com.digital.ledger.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.digital.admin.mapper.SysDictItemMapper;
import com.digital.admin.service.SysUserService;
import com.digital.common.core.constant.CommonConstants;
import com.digital.common.core.exception.ApplicationException;
import com.digital.common.core.exception.CheckedException;
import com.digital.doc.helper.UserHelper;
import com.digital.ledger.helper.LedgerAssignerHelper;
import com.digital.ledger.helper.LedgerNotificationHelper;
import com.digital.ledger.mapper.LedgerTemplateMapper;
import com.digital.ledger.service.IImportLedgerService;
import com.digital.ledger.service.ILedgerNewRowService;
import com.digital.ledger.service.LedgerColumnService;
import com.digital.ledger.service.LedgerRowService;
import com.digital.ledger.service.LedgerService;
import com.digital.ledger.util.BaseUtils;
import com.digital.ledger.util.Constants;
import com.digital.model.admin.entity.SysUser;
import com.digital.model.ledger.entity.LedgerColumnEntity;
import com.digital.model.ledger.entity.LedgerTemplateEntity;
import com.digital.model.ledger.entity.LedgerEntity;
import com.digital.model.ledger.entity.LedgerNewRowEntity;
import com.digital.model.ledger.entity.LedgerRowEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional(rollbackFor = Exception.class)
public class ImportLedgerServiceImpl implements IImportLedgerService {
    private static final Logger LOGGER = LogManager.getLogger(ImportLedgerServiceImpl.class);

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private SysDictItemMapper sysDictItemMapper;

    @Autowired
    private LedgerTemplateMapper templateMapper;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private LedgerColumnService ledgerColumnService;

    @Autowired
    private LedgerRowService ledgerRowService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ILedgerNewRowService ledgerNewRowService;

    @Autowired
    private LedgerNotificationHelper ledgerNotificationHelper;

    @Autowired
    private LedgerAssignerHelper assignerHelper;

    /**
     * 台账导入（分发）
     *
     * @param file 文件
     * @param templateId 模板id
     */
    @Override
    public void readExcel(MultipartFile file, Long templateId) {
        String fileName = file.getOriginalFilename();
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        String tempId = IdUtil.fastSimpleUUID();
        String uuidFileName = tempId + "." + fileType;
        String filePath = null;
        if (!Objects.equals(templateId, 0L)) {
            LedgerTemplateEntity templateEntity = templateMapper.selectById(templateId);
            if (Objects.nonNull(templateEntity)) {
                // 台账文件路径
                filePath = String.join("/", sysDictItemMapper.findItemValueByType(CommonConstants.LEDGER_FILE_PATH), DateUtil.today(), uuidFileName);
                FileUtil.copyFile(templateEntity.getPath(), filePath);
            } else {
                throw new CheckedException("id：【"+ templateId +"】的模板不存在！");
            }
        }

        try (FileInputStream inputStream = (FileInputStream) file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // 读取第一个工作表
            String sheetName = sheet.getSheetName();
            // 1、保存创建人上传模板
            LedgerEntity ledgerEntity;
            LedgerColumnEntity columnEntity;
            // 第二列为【指派人(工号)*】类型赋值为1
            int tempType = getTempType(sheet);
            ledgerEntity = insertLedger(tempId, templateId, fileName, tempType, sheetName, filePath);
            // 模板列id对象
            ConcurrentHashMap<Long, Long> columnIdMap = new ConcurrentHashMap<>();
            // 存储工号/数据
            List<String> cellValueList = new ArrayList<>();
            for (Row row : sheet) { // 迭代每一行
                // 存储相同的行工号
                ConcurrentHashMap<Long, String> cpRowIndexMap = new ConcurrentHashMap<>();
                // 存储list对象列坐标，用于判断列是否为自增1的场景（限制空列名情况）
                List<Long> columnIndexList = new ArrayList<>();
                for (Cell cell : row) { // 迭代每一列
                    // 列坐标
                    long columnIndex = cell.getColumnIndex();
                    // 行坐标
                    long rowIndex = cell.getRowIndex();
                    DataFormatter formatter = new DataFormatter();
                    String stringCellValue = formatter.formatCellValue(cell);
                    /*     LOGGER.info("{} STRING c:【{}】l:【{}】\t", stringCellValue, columnIndex, rowIndex);*/
                    if (rowIndex != 0) {
                        // 1、按行模板导入类型
                        if (tempType == 1) {
                            // 保存列数据
                            if (rowIndex == 1) {
                                // 检查是否存在样式的空列名
                                BaseUtils.checkColumnIsEmpty(stringCellValue, columnIndex);
                                columnIndexList.add(columnIndex);
                                // 插入列名
                                columnEntity = insertColumn(tempId, columnIndex, stringCellValue);
                                columnIdMap.put(columnIndex, columnEntity.getId());
                            } else {
                                // 存储指派人(工号)*列
                                if (columnIndex == 0) {
                                    cellValueList.add(stringCellValue);
                                }
                                // 保存非列名的行数据
                                insertTypeOneRow(tempId, tempType, columnIdMap, columnIndex, rowIndex, stringCellValue, cpRowIndexMap);
                            }

                        }

                        // 2、按单元格类型
                        if (tempType == 2) {
                            // 保存列数据
                            if (rowIndex == 1) {
                                // 检查是否存在样式的空列名
                                BaseUtils.checkColumnIsEmpty(stringCellValue, columnIndex);
                                // 插入列名
                                columnEntity = insertColumn(tempId, columnIndex, stringCellValue);
                                columnIdMap.put(columnIndex, columnEntity.getId());
                            } else {
                                for (Map.Entry<Long, Long> entry : columnIdMap.entrySet()) {
                                    if (entry.getKey().equals(columnIndex)) {
                                        // 保存非列名的行数据
                                        insertTypeTwoRow(tempId, tempType, entry.getValue(), columnIndex, rowIndex, stringCellValue, cellValueList);
                                    }
                                }
                            }
                        }
                    }
                }
                // 检查是否存在样式的空列名
                BaseUtils.isIncreasing(columnIndexList);
            }
            // 校验工号
            if (tempType == 1) {
                isUser(cellValueList);
            }
            ledgerNotificationHelper.sendNotification(ledgerEntity.getName(), cellValueList, true);

            // 记录单元格数量
            assignerHelper.insertLedgerAssigners(tempId, tempType, cellValueList, columnIdMap.size() - 2);
        } catch (IOException e) {
            LOGGER.error(e);
            throw new ApplicationException("台账数据上传失败!");
        }
    }

    private static int getTempType(Sheet sheet) {
        Row TowRow = sheet.getRow(1);
        if (Objects.isNull(TowRow)) {
            throw new CheckedException("导入的台账格式不正确!");
        }
        DataFormatter formatter = new DataFormatter();
        String assignerCellStrValue = formatter.formatCellValue(TowRow.getCell(0));
        int tempType;
        if (Constants.ASSIGNER.equals(assignerCellStrValue)) {
            tempType = 1;
        } else if (Constants.EXCEL_TITLE.equals(assignerCellStrValue)) {
            tempType = 2;
        } else {
            throw new CheckedException("导入的台账格式不正确!");
        }
        return tempType;
    }

    // 保存模板
    private LedgerEntity insertLedger(String tempId, Long templateId, String fileName, Integer tempType, String sheetName, String filePath) {
        LedgerEntity ledgerEntity = new LedgerEntity();
        ledgerEntity.setTempId(tempId);
        ledgerEntity.setTemplateId(templateId);
        ledgerEntity.setName(fileName);
        ledgerEntity.setSheetName(sheetName);
        ledgerEntity.setType(tempType);
        ledgerEntity.setPath(filePath);
        ledgerEntity.setCreateById(userHelper.getUserId());
        ledgerEntity.setStatus(1);
        ledgerEntity.setDelFlag(1);
        ledgerService.save(ledgerEntity);
        return ledgerEntity;
    }

    // 保存列名
    private LedgerColumnEntity insertColumn(String tempId, Long columnIndex, String value) {
        LedgerColumnEntity columnEntity = new LedgerColumnEntity();
        columnEntity.setTempId(tempId);
        columnEntity.setName(value);
        columnEntity.setSort(columnIndex.intValue());
        if (value.equals(Constants.EXCEL_TITLE) || value.equals(Constants.ASSIGNER)) {
            columnEntity.setIsFixed(1);
        }else {
            columnEntity.setIsFixed(0);
        }
        columnEntity.setCreateById(userHelper.getUserId());
        /*LOGGER.info("\n\t" + "columnEntity: {}", columnEntity + "\n\t");*/
        ledgerColumnService.save(columnEntity);
        return columnEntity;
    }

    // 保存按行分发坐标
    private void insertTypeOneRow(String tempId, Integer tempType, ConcurrentHashMap<Long, Long> columnIdMap, Long columnIndex, Long rowIndex, String value, ConcurrentHashMap<Long, String> cpRowIndexMap) {
        if (StringUtils.isBlank(value) && columnIndex > 1) {
            return;
        }
        LedgerRowEntity rowEntity = new LedgerRowEntity();
        rowEntity.setTempId(tempId);
        rowEntity.setType(tempType);
        rowEntity.setColumnId(columnIdMap.get(columnIndex));
        rowEntity.setRowId(rowIndex -1);
        // 固定列设置可见但不可编辑
        if (columnIndex == 0 || columnIndex == 1) {
            rowEntity.setIsEdit(0);
            rowEntity.setIsView(1);
        } else {
            rowEntity.setIsView(0);
            rowEntity.setIsEdit(1);
        }
        // 指派人列存储工号
        if (rowIndex != 0 && columnIndex == 0) {
            rowEntity.setAssigner(value);
            rowEntity.setContent(value);
            cpRowIndexMap.put(rowIndex, value);
        } else {
            // 行标题存为内容
            rowEntity.setContent(value);
            if (!cpRowIndexMap.isEmpty()) {
                for (Map.Entry<Long, String> cp : cpRowIndexMap.entrySet()) {
                    if (cp.getKey().equals(rowIndex)) {
                        rowEntity.setAssigner(cp.getValue());
                    }
                }
            }
        }
        rowEntity.setCreateById(userHelper.getUserId());
        /*LOGGER.info("\n\t" +"rowEntity: {}", rowEntity + "\n\t");*/
        ledgerRowService.save(rowEntity);
    }

    // 保存单元格坐标
    private void insertTypeTwoRow(String tempId, Integer tempType, Long columnId, Long columnIndex, Long rowIndex, String value, List<String> cellValueList) {
        LedgerRowEntity typeTwoRowEntity = new LedgerRowEntity();
        typeTwoRowEntity.setTempId(tempId);
        typeTwoRowEntity.setType(tempType);
        typeTwoRowEntity.setColumnId(columnId);
        typeTwoRowEntity.setRowId(rowIndex- 1);
        // 固定列设置不可编辑
        if (columnIndex == 0) {
            typeTwoRowEntity.setIsEdit(0);
        }else {
            typeTwoRowEntity.setIsEdit(1);
        }
        // 不是固定列存储编至辑人字段
        if (rowIndex != 0 && columnIndex != 0) {
            SysUser user = sysUserService.getOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, value));
            if (Objects.isNull(user)) {
                // 创建人上传台账数据非固定列且非用户工号，设置仅不可见、可编辑
                typeTwoRowEntity.setIsView(0);
                typeTwoRowEntity.setIsEdit(1);
                typeTwoRowEntity.setContent(value);
                typeTwoRowEntity.setAssigner(userHelper.getUser().getName());
            } else {
                typeTwoRowEntity.setIsView(1);
                typeTwoRowEntity.setIsEdit(1);
                typeTwoRowEntity.setAssigner(value);
                cellValueList.add(value);
            }
        } else {
            // 创建人上传台账固定列设置可见
            typeTwoRowEntity.setIsView(1);
            // 行标题存为内容
            typeTwoRowEntity.setIsEdit(0);
            typeTwoRowEntity.setContent(value);
            typeTwoRowEntity.setAssigner(userHelper.getUser().getName());
        }
        typeTwoRowEntity.setCreateById(userHelper.getUserId());
       /* LOGGER.info("\n\t" +"typeTwoRowEntity: {}", typeTwoRowEntity + "\n\t");*/
        ledgerRowService.save(typeTwoRowEntity);
    }

    // 判断用户名是否存在
    private void isUser(List<String> cellValueList) {
        List<Object> errorUsers = new ArrayList<>();
        for (String userName : cellValueList) {
            SysUser sysUser = sysUserService.getOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, userName));
            if (Objects.isNull(sysUser)) {
                errorUsers.add(userName);
            }
        }
        if (!errorUsers.isEmpty()) {
            throw new CheckedException("用户"+ errorUsers +"不存在！");
        }
    }

    @Override
    public void editorUpload(MultipartFile file, String tempId) {
        List<Long> columnId = new ArrayList<>();
        Map<Long,Long> columnIdMap = new HashMap<>();
        Map<String, String> assignerMap = new HashMap<>();
        LedgerEntity ledgerEntity = ledgerService.findByTempId(tempId);
        if (Objects.isNull(ledgerEntity)) {
            throw new CheckedException("台账不存在，请重新上传！");
        }
        int type = ledgerEntity.getType();
        if (type == 1) {
            try (InputStream inputStream = file.getInputStream();
                 Workbook workbook = new XSSFWorkbook(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0); // 读取第一个工作表
                checkLedgerFormat(sheet, type);
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        // 列坐标
                        long columnIndex = cell.getColumnIndex();
                        // 行坐标
                        long rowIndex = cell.getRowIndex();
                        rowCellType(columnId, tempId, type, rowIndex, columnIndex, cell, assignerMap);
                    }
                }
            } catch (IOException e) {
                LOGGER.error(e);
                throw new ApplicationException("按行模板，文件读取失败！");
            }
        } else {
            try (InputStream inputStream = file.getInputStream();
                 Workbook workbook = new XSSFWorkbook(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0); // 读取第一个工作表
                checkLedgerFormat(sheet, type);
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        // 列坐标
                        long columnIndex = cell.getColumnIndex();
                        // 行坐标
                        long rowIndex = cell.getRowIndex();
                        cellType(columnIdMap, tempId, rowIndex, columnIndex, cell);
                    }
                }
            } catch (IOException e) {
                LOGGER.error(e);
                throw new ApplicationException("单元格模板文件读取失败！");
            }
        }

        // 更新用户填写的单元格数量
        assignerHelper.updateLedgerAssigners(tempId);
    }

    private static void checkLedgerFormat(Sheet sheet, int type) {
        Row row = sheet.getRow(0);
        if (Objects.isNull(row)) {
            throw new CheckedException("导入的台账格式不正确!");
        }
        Cell firstCell = row.getCell(0);
        DataFormatter formatter = new DataFormatter();
        String firstCellValue = formatter.formatCellValue(firstCell);
        if (type == 1) {
            if (!StringUtils.equals(firstCellValue, Constants.ASSIGNER)) {
                throw new CheckedException("导入的台账格式不正确!");
            }
        } else if (type == 2) {
            if (!StringUtils.equals(firstCellValue, Constants.EXCEL_TITLE)) {
                throw new CheckedException("导入的台账格式不正确!");
            }
        }
    }

    // 保存按行模版内容数据
    private void rowCellType(List<Long> columnId, String tempId, Integer type, Long rowIndex, Long columnIndex, Cell cell, Map<String, String> assignerMap) {
        DataFormatter formatter = new DataFormatter();
        String cellValue = formatter.formatCellValue(cell);
        // 检查列名与创建人导入的模板是否一致
        cpColumnName(tempId, rowIndex, columnIndex, cellValue);
        if (columnIndex == 0) {
            LedgerColumnEntity column = ledgerColumnService.getOne(
                    Wrappers.<LedgerColumnEntity>lambdaQuery().
                            eq(LedgerColumnEntity::getTempId, tempId)
                            .eq(LedgerColumnEntity::getSort, columnIndex));
            if(cellValue.equals(Constants.ASSIGNER)) {
                columnId.add(column.getId());
            }
            assignerMap.put("assigner", cellValue);
        }

        if (columnIndex != 0 && columnIndex != 1) {
            LedgerColumnEntity columnEntity = ledgerColumnService.getOne(
                    Wrappers.<LedgerColumnEntity>lambdaQuery().
                            eq(LedgerColumnEntity::getTempId, tempId)
                            .eq(LedgerColumnEntity::getSort, columnIndex));
            if (Objects.nonNull(columnEntity)) {
                if (rowIndex != 0 && (StringUtils.isNotEmpty(cellValue) || !cell.getCellStyle().getLocked())) {
                    if (!assignerMap.get("assigner").equals(userHelper.getUser().getName())) {
                        throw new CheckedException("台账解析异常，指派人与当前登录人不一致！");
                    }
                    LedgerNewRowEntity newRow = ledgerNewRowService.getOne(
                            Wrappers.<LedgerNewRowEntity>lambdaQuery().
                                    eq(LedgerNewRowEntity::getTempId, tempId)
                                    .eq(LedgerNewRowEntity::getColumnId, columnEntity.getId())
                                    .eq(LedgerNewRowEntity::getNewRowId, rowIndex).eq(LedgerNewRowEntity::getAssigner, assignerMap.get("assigner")));
                    if (Objects.nonNull(newRow)) {
                        ledgerRowService.update(Wrappers.<LedgerRowEntity>lambdaUpdate()
                                .set(LedgerRowEntity::getContent, cellValue)
                                .eq(LedgerRowEntity::getColumnId, newRow.getColumnId())
                                .eq(LedgerRowEntity::getRowId, newRow.getOldRowId())
                                .eq(LedgerRowEntity::getAssigner, userHelper.getUser().getName()));
                    } else {
                        List<LedgerRowEntity> ledgerRowEntity = ledgerRowService.findRowList(tempId, columnId.get(0), userHelper.getUser().getName());
                        if (CollectionUtils.isEmpty(ledgerRowEntity)) {
                            throw new CheckedException("按行模版，获取ledgerRowEntity异常！");
                        }
                        LedgerRowEntity rowEntity = ledgerRowService.getOne(Wrappers.<LedgerRowEntity>lambdaQuery().eq(LedgerRowEntity::getTempId, columnEntity.getTempId())
                                .eq(LedgerRowEntity::getType, type).eq(LedgerRowEntity::getColumnId, columnEntity.getId())
                                .eq(LedgerRowEntity::getRowId, ledgerRowEntity.get(Integer.parseInt(String.valueOf(rowIndex)) - 1).getRowId())
                                .eq(LedgerRowEntity::getAssigner, userHelper.getUser().getName()));
                        if (Objects.isNull(rowEntity)) {
                            rowEntity = new LedgerRowEntity();
                            rowEntity.setTempId(columnEntity.getTempId());
                            rowEntity.setType(type);
                            rowEntity.setColumnId(columnEntity.getId());
                            rowEntity.setRowId(ledgerRowEntity.get(Integer.parseInt(String.valueOf(rowIndex)) - 1).getRowId());
                            rowEntity.setAssigner(userHelper.getUser().getName());
                            rowEntity.setContent(cellValue);
                            rowEntity.setIsView(1);
                            rowEntity.setIsEdit(1);
                            rowEntity.setCreateById(userHelper.getUserId());
                            ledgerRowService.save(rowEntity);
                        } else {
                            rowEntity.setContent(cellValue);
                            ledgerRowService.updateById(rowEntity);
                        }
                    }
                }
                LOGGER.info("行模板，cellValue:{}", cellValue);
            }
        }
    }

    // 保存单元格模板内容数据
    private void cellType(Map<Long, Long> columnIdMap, String tempId, Long rowIndex, Long columnIndex, Cell cell) {
        DataFormatter formatter = new DataFormatter();
        String cellValue = formatter.formatCellValue(cell);
        if (columnIndex != 0) {
            if (rowIndex == 0) {
                LedgerColumnEntity column = ledgerColumnService.getOne(
                        Wrappers.<LedgerColumnEntity>lambdaQuery().
                                eq(LedgerColumnEntity::getTempId, tempId)
                                .eq(LedgerColumnEntity::getName, cellValue));
                if (Objects.nonNull(column)){
                    columnIdMap.put(columnIndex, column.getId());
                } else {
                    LOGGER.info("tempId:{}", tempId);
                    throw new CheckedException("列名：'" + cellValue + "'不存在！");
                }
            } else {
                if (StringUtils.isNotEmpty(cellValue) || !cell.getCellStyle().getLocked()) {
                    LedgerNewRowEntity newRow = ledgerNewRowService.getOne(
                            Wrappers.<LedgerNewRowEntity>lambdaQuery().
                                    eq(LedgerNewRowEntity::getTempId, tempId)
                                    .eq(LedgerNewRowEntity::getColumnId, columnIdMap.get(columnIndex))
                                    .eq(LedgerNewRowEntity::getNewRowId, rowIndex)
                                    .eq(LedgerNewRowEntity::getAssigner, userHelper.getUser().getName()));
                    if (Objects.nonNull(newRow)) {
                        if (!newRow.getAssigner().equals(userHelper.getUser().getName())) {
                            throw new CheckedException("台账解析异常，指派人与当前登录人不一致！");
                        }
                    } else {
                        LOGGER.info("获取ledgerRowEntity异常！tempId:{}", tempId);
                        throw new CheckedException("导入的台账不一致，请检查！");
                    }
                    ledgerRowService.update(Wrappers.<LedgerRowEntity>lambdaUpdate()
                            .set(LedgerRowEntity::getContent, cellValue)
                            .eq(LedgerRowEntity::getColumnId, newRow.getColumnId())
                            .eq(LedgerRowEntity::getRowId, newRow.getOldRowId()));
                }
                LOGGER.info("单元格模板，cellValue:{}", cellValue);
            }
        }
    }

    private void cpColumnName(String tempId, Long rowIndex, Long columnIndex, String cellValue) {
        if (rowIndex == 0) {
            LedgerColumnEntity columnName = ledgerColumnService.getOne(
                    Wrappers.<LedgerColumnEntity>lambdaQuery().
                            eq(LedgerColumnEntity::getTempId, tempId)
                            .eq(LedgerColumnEntity::getName, cellValue)
                            .eq(LedgerColumnEntity::getSort, columnIndex));
            if (Objects.isNull(columnName)) {
                throw new CheckedException("导入的台账列名:'" + cellValue + "'不匹配，请检查！");
            }
        }
    }
}
