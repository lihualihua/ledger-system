package com.digital.ledger.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.digital.common.core.exception.ApplicationException;
import com.digital.common.core.exception.CheckedException;
import com.digital.doc.config.DocFileConfig;
import com.digital.doc.enums.OwnershipEnum;
import com.digital.doc.helper.FileHelper;
import com.digital.doc.helper.UserHelper;
import com.digital.doc.service.IDocFileService;
import com.digital.doc.service.IDocQueryService;
import com.digital.doc.service.IDownloadService;
import com.digital.ledger.enums.LedgerStatusEnum;
import com.digital.ledger.enums.LedgerTypeEnum;
import com.digital.ledger.helper.LedgerNotificationHelper;
import com.digital.ledger.mapper.LedgerAssignerMapper;
import com.digital.ledger.mapper.LedgerNewRowMapper;
import com.digital.ledger.service.ILedgerOperateService;
import com.digital.ledger.service.LedgerColumnService;
import com.digital.ledger.service.LedgerRowService;
import com.digital.ledger.service.LedgerService;
import com.digital.ledger.service.LedgerTemplateService;
import com.digital.ledger.util.Constants;
import com.digital.model.doc.dto.DocFileDto;
import com.digital.model.doc.entity.DocFileEntity;
import com.digital.model.ledger.dto.TemplateDto;
import com.digital.model.ledger.entity.LedgerAssignerEntity;
import com.digital.model.ledger.entity.LedgerColumnEntity;
import com.digital.model.ledger.entity.LedgerEntity;
import com.digital.model.ledger.entity.LedgerNewRowEntity;
import com.digital.model.ledger.entity.LedgerRowEntity;
import com.digital.model.ledger.entity.LedgerTemplateEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional
public class LedgerOperateServiceImpl implements ILedgerOperateService {
    private static final Logger LOGGER = LogManager.getLogger(LedgerOperateServiceImpl.class);

    @Autowired
    private DocFileConfig fileConfig;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private IDownloadService downloadService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private LedgerColumnService columnService;

    @Autowired
    private LedgerRowService rowService;

    @Autowired
    private LedgerNewRowMapper newRowMapper;

    @Autowired
    private IDocQueryService queryService;

    @Autowired
    private FileHelper fileHelper;

    @Autowired
    private IDocFileService docFileService;

    @Autowired
    private LedgerNotificationHelper ledgerNotificationHelper;

    @Autowired
    private LedgerAssignerMapper assignerMapper;

    @Autowired
    private LedgerTemplateService templateService;

    @Override
    public void archive(LedgerEntity ledgerEntity, boolean ignoreCheck) {

        // 检查是否全部填写完成
        String tempId = ledgerEntity.getTempId();

        // 检查所有分配人都已编辑或导入数据
//        checkAssigners(tempId, ignoreCheck);

        // 存储文件
        DocFileEntity fileEntity = null;
        if (Objects.isNull(ledgerEntity.getTemplateId()) || Objects.equals(ledgerEntity.getTemplateId(), 0L)
                || StringUtils.isBlank(ledgerEntity.getPath())) {
            fileEntity = generateLedgerExcel(ledgerEntity, 0,true);
        } else {
            fileEntity = writeOriginalLedgerExcel(tempId, ledgerEntity.getPath(), ledgerEntity);
        }

        // 更新台账信息
        ledgerEntity.setStatus(LedgerStatusEnum.ARCHIVED.getCode());
        ledgerEntity.setFileId(fileEntity.getFileId());
        ledgerEntity.setPath(fileEntity.getFilePath());
        ledgerService.updateById(ledgerEntity);

        // 删除台账记录列，行数据
        deleteColumnRowData(tempId);
    }

    private void checkAssigners(String tempId, boolean ignoreCheck) {
        if (ignoreCheck) {
            return;
        }
        List<LedgerAssignerEntity> assigners = assignerMapper.selectList(
                Wrappers.<LedgerAssignerEntity>lambdaQuery().eq(LedgerAssignerEntity::getTempId, tempId).eq(LedgerAssignerEntity::getStatus, 1));
        if (CollectionUtils.isEmpty(assigners)) {
            return;
        }
        String usernames = assigners.stream().map(LedgerAssignerEntity::getAssigner).collect(Collectors.toSet()).toString();
        throw new CheckedException("该台账还有部分用户" + usernames + "未处理，请确认是否强制归档？");
    }

    private DocFileEntity writeOriginalLedgerExcel(String tempId, String path, LedgerEntity ledgerEntity) {
        try (FileInputStream inputStream = new FileInputStream(path);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {

            // 查找表格中的列数据
            List<LedgerColumnEntity> titleList = getLedgerColumnEntities(tempId, 0);
            if (CollectionUtils.isEmpty(titleList)) {
                LOGGER.error("台账列数据为空，tempId:{}", tempId);
                throw new ApplicationException("归档失败，请联系管理员!");
            }

            // 获取sheet页
            Sheet sheet = workbook.getSheetAt(0);

            // 设置工作表保护
            sheet.protectSheet(fileConfig.getExcelPassword());

            // 设置列数据
            Map<Long, Integer> titleIndexMap = new HashMap<>();
            AtomicInteger titleIndex = new AtomicInteger(0);
            // 列下标-列名
            titleList.forEach(column -> {
                int indexValue = titleIndex.getAndIncrement();
                titleIndexMap.put(column.getId(), indexValue);
            });

            // 根据tempid查找表格中的数据，根据当前用户过滤数据，如果是管理员，则查全部
            List<LedgerRowEntity> rows = getLedgerRowEntities(userHelper.getUser().getName(), ledgerEntity, tempId, 0);
            if (CollectionUtils.isEmpty(rows)) {
                LOGGER.error("台账行数据为空，tempId:{}", tempId);
                throw new ApplicationException("归档失败，请联系管理员!");
            }

            // 设置行数据
            Map<Long, List<LedgerRowEntity>> rowsMap = rows.stream().collect(Collectors.groupingBy(LedgerRowEntity::getRowId));
            rowsMap.keySet().stream().sorted().forEach(key -> {
                int rowId = Math.toIntExact(key);
                Row ledgerRow = Objects.isNull(sheet.getRow(rowId)) ? sheet.createRow(rowId) : sheet.getRow(rowId);
                rowsMap.get(key).forEach(item -> {
                    // 获取列下标
                    Long columnId = item.getColumnId();
                    int indexValue = titleIndexMap.get(columnId);
                    Cell cell = Objects.isNull(ledgerRow.getCell(indexValue)) ? ledgerRow.createCell(indexValue) : ledgerRow.getCell(indexValue);
                    cell.setCellValue(item.getContent());
                });
            });

            // 存储到文件中
            saveWorkbook(path, workbook);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ApplicationException(e);
        }
        File file = new File(path);
        return docFileService.createFile(buildArchiveFileEntity(file, ledgerEntity.getName()), null);
    }

    private void deleteColumnRowData(String tempId) {
        columnService.remove(Wrappers.<LedgerColumnEntity>lambdaQuery().eq(LedgerColumnEntity::getTempId, tempId));
        rowService.remove(Wrappers.<LedgerRowEntity>lambdaQuery().eq(LedgerRowEntity::getTempId, tempId));
        newRowMapper.delete(Wrappers.<LedgerNewRowEntity>lambdaQuery().eq(LedgerNewRowEntity::getTempId, tempId));
    }

    @Override
    public void revoke(LedgerEntity ledgerEntity) {
        ledgerEntity.setStatus(LedgerStatusEnum.VOIDED.getCode());
        ledgerEntity.setDelFlag(0);
        ledgerService.updateById(ledgerEntity);
    }

    @Override
    public void delete(LedgerEntity ledgerEntity) {
        ledgerService.removeById(ledgerEntity.getId());

        // 删除台账记录列，行数据
        deleteColumnRowData(ledgerEntity.getTempId());
    }

    @Override
    public void urge(LedgerEntity ledgerEntity) {
        List<LedgerAssignerEntity> assigners = assignerMapper.selectList(Wrappers.<LedgerAssignerEntity>lambdaQuery().eq(
                LedgerAssignerEntity::getTempId, ledgerEntity.getTempId()).eq(LedgerAssignerEntity::getStatus, 1));
        if (CollectionUtils.isEmpty(assigners)) {
            return;
        }
        List<String> usernames = assigners.stream().map(LedgerAssignerEntity::getAssigner).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(usernames)) {
            return;
        }
        ledgerNotificationHelper.sendNotification(ledgerEntity.getName(), usernames, false);
    }

    @Override
    public DocFileEntity generateLedgerExcel(LedgerEntity ledgerEntity, Integer fixed, boolean isArchive) {
        String tempId = ledgerEntity.getTempId();
        String username = userHelper.getUser().getUsername();
        boolean isCreator = Objects.equals(username, ledgerEntity.getCreateBy());
        // 根据表格名称生成excel，名称规则为：xxx_用户工号_日期.xlsx
        String fileType = ".xlsx";
        String uuidName = IdUtil.fastSimpleUUID() + fileType;
        String fileName = getFileName(ledgerEntity.getName(), username, fileType, isCreator);
        String filePath = getFilePath(username, uuidName, isArchive);

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                FileUtil.touch(filePath);
            }

            // 查找表格中的列数据
            List<LedgerColumnEntity> titleList = getLedgerColumnEntities(tempId, fixed);
            if (CollectionUtils.isEmpty(titleList)) {
                // 没有列信息，直接下载空文件
                throw new ApplicationException("该台账数据异常，归档失败！");
            }

            // 创建工作簿和sheet页
            Workbook workbook = new XSSFWorkbook();
            String sheetName = StringUtils.defaultIfBlank(ledgerEntity.getSheetName(), "台账数据管理");
            Sheet sheet = workbook.createSheet(sheetName);
            setSheetProperties(sheet);

            // 设置列数据
            Map<Long, Integer> titleIndexMap = new HashMap<>();
            setTitleData(sheet, titleList, titleIndexMap);

            // 根据tempid查找表格中的数据，根据当前用户过滤数据，如果是管理员，则查全部
            List<LedgerRowEntity> rows = getLedgerRowEntities(username, ledgerEntity, tempId, fixed);

            // 设置行数据
            setRowData(rows, sheet, titleIndexMap, ledgerEntity, isArchive);

            // 写入文件
            saveWorkbook(filePath, workbook);

            if (isArchive) {
                return docFileService.createFile(buildArchiveFileEntity(file, fileName), null);
            }
        } catch (Exception ex) {
            LOGGER.error("下载用户模板异常，用户工号：{}，模板ID：{}", username, tempId);
            LOGGER.error(ex);
            throw new ApplicationException("生成台账Excel异常，请联系管理员!");
        }

        return buildFileEntity(fileName, filePath);
    }

    private DocFileEntity buildArchiveFileEntity(File file, String fileName) {
        DocFileEntity docFileEntity = new DocFileEntity();
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        docFileEntity.setFileName(fileName);
        docFileEntity.setFileType(fileType);
        docFileEntity.setFileSize(file.length());
        docFileEntity.setFileSizeStr(FileUtil.readableFileSize(file.length()));
        docFileEntity.setFilePath(file.getPath());
        docFileEntity.setDelFlag(1);
        docFileEntity.setOwnership(OwnershipEnum.DEPT.getCode());
        docFileEntity.setIcon(fileHelper.getFileIcon(null, fileType));
        docFileEntity.setDownloadCount(0);
        docFileEntity.setPreviewCount(0);
        docFileEntity.setCollectCount(0);
        docFileEntity.setFolderFlag(0);
        docFileEntity.setEditFlag(2);

        // 查找归档目录
        DocFileDto fileDto = new DocFileDto();
        fileDto.setFileName("归档文件");
        fileDto.setFolderFlag(1);
        fileDto.setParentId(0L);
        fileDto.setDelFlag(1);
        fileDto.setOwnership(OwnershipEnum.DEPT.getCode());
        DocFileEntity parentFile = queryService.findDocFileEntity(fileDto);
        if (Objects.isNull(parentFile)) {
            throw new ApplicationException("该部门未创建归档目录，请联系管理员!");
        }
        docFileEntity.setParentId(parentFile.getFileId());
        return docFileEntity;
    }

    @Override
    public void downloadLedgerExcel(TemplateDto template, HttpServletResponse response) {
        // 根据tempId查找表格信息
        String tempId = template.getTempId();
        LedgerEntity templateEntity = ledgerService.findByTempId(tempId);
        if (Objects.isNull(templateEntity)) {
            LOGGER.error("台账模板不存在，模板ID：{}", tempId);
            throw new ApplicationException("台账数据不存在，请联系管理员!");
        }

        if (Objects.equals(templateEntity.getStatus(), LedgerStatusEnum.ARCHIVED.getCode())) {
            downloadService.download(queryService.findDelById(templateEntity.getFileId(), false), response);
        } else {
            DocFileEntity fileEntity = generateLedgerExcel(templateEntity, template.getIsFixed(),false);
            downloadService.download(fileEntity, response);
        }
    }

    private DocFileEntity buildFileEntity(String fileName, String filePath) {
        DocFileEntity fileEntity = new DocFileEntity();
        fileEntity.setFileName(fileName);
        fileEntity.setFilePath(filePath);
        return fileEntity;
    }

    private List<LedgerColumnEntity> getLedgerColumnEntities(String tempId, Integer isFixed) {
        LedgerColumnEntity ledgerColumn = new LedgerColumnEntity();
        ledgerColumn.setTempId(tempId);
        ledgerColumn.setIsFixed(isFixed);
        return columnService.findLedgerColumnList(ledgerColumn);
    }

    private List<LedgerRowEntity> getLedgerRowEntities(String username, LedgerEntity ledgerEntity, String tempId, Integer isFixed) {
        if (StringUtils.equals(username, ledgerEntity.getCreateBy())) {
            return rowService.findAllRows(tempId, isFixed);
        }

        return rowService.findRowsByUser(tempId, isFixed);
    }

    @Override
    public void downloadLedgerTemplate(Long id, Integer type, HttpServletResponse response) {
        LedgerTemplateEntity templateEntity = templateService.getById(id);
        if (Objects.isNull(templateEntity)) {
            throw new ApplicationException("台账模板不存在，请联系管理员!");
        }
        String templatePath = templateEntity.getPath();
        String newFilePath = null;
        // 如果是系统自带模板直接下载
        String[] pathArr = templatePath.split("/");
        pathArr[pathArr.length - 1] = type + "_" + pathArr[pathArr.length - 1];
        if (Objects.equals(templateEntity.getSystemFlag(), 1)) {
            newFilePath = StringUtils.join(pathArr, "/");
        } else {
            newFilePath = String.join("/", fileConfig.getTmpPath(), DateUtil.today(), pathArr[pathArr.length - 1]);
        }
        File file = new File(newFilePath);
        DocFileEntity fileEntity = new DocFileEntity();
        fileEntity.setFileName(templateEntity.getName());
        fileEntity.setFilePath(file.getPath());
        if (!file.exists()) {
            FileUtil.copyFile(templatePath, newFilePath);
            writeExcelTemplate(type, newFilePath);
        }
        downloadService.download(fileEntity, response);
    }

    private void writeExcelTemplate(Integer type, String newFilePath) {
        int shiftColumnNumber = type == 1 ? 2 : 1;
        try (FileInputStream inputStream = new FileInputStream(newFilePath);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // 获取第一个工作表
            int firstRowNum = sheet.getFirstRowNum();
            Row firstRow = sheet.getRow(firstRowNum);
            int firstCellNum = Objects.nonNull(firstRow) ? firstRow.getFirstCellNum() : 0;
            int lastCellNum = Objects.nonNull(firstRow) ? firstRow.getLastCellNum() : 0;

            // 向后移动列，为新列腾出空间
            sheet.shiftColumns(firstCellNum, lastCellNum, shiftColumnNumber);

            // 插入两列，从最前面开始插入（即列号0和1）
            addNewColumn(type, firstRow, sheet);

            // 指定分配人编辑区域
            int lastRowNum = Math.max(sheet.getPhysicalNumberOfRows(), 10);
            int cellCount = lastCellNum + shiftColumnNumber;
            IntStream.range(1, lastRowNum).forEach(index -> {
                Row row = Objects.isNull(sheet.getRow(index)) ? sheet.createRow(index) : sheet.getRow(index);
                // shiftColumnNumber
                IntStream.range(0, cellCount).forEach(cellIndex -> {
                    Cell cell = Objects.isNull(row.getCell(cellIndex)) ? row.createCell(cellIndex) : row.getCell(cellIndex);
                    XSSFCellStyle style = (XSSFCellStyle) sheet.getWorkbook().createCellStyle();
                    if (type == 1) {
                        if (cellIndex <= shiftColumnNumber - 1) {
                            setForegroundColor(style);
                        }
                    } else {
                        setForegroundColor(style);
                    }
                    setBorder(style);
                    cell.setCellStyle(style);
                });
            });

            // 插入提示
            sheet.shiftRows(0, lastRowNum, 1);
            Row newRow = sheet.createRow(0);
            newRow.setHeight((short) (20 * 90));
            Cell newCell = newRow.createCell(0);
            String content = getContent(type);
            newCell.setCellValue(content);
            XSSFCellStyle style = (XSSFCellStyle) sheet.getWorkbook().createCellStyle();
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            style.setWrapText(true);
            newCell.setCellStyle(style);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, cellCount - 1));

            // 创建示例sheet
            createExampleSheet(workbook, type, shiftColumnNumber);

            // 存储到文件中
            saveWorkbook(newFilePath, workbook);
        } catch (IOException e) {
            LOGGER.error(e);
            throw new ApplicationException("下载模板失败，请联系管理员！");
        }
    }

    private static String getContent(Integer type) {
        String content;
        if (type == 1) {
            content = "注意事项：\n 1. 【分配人域账号】和【行标题（非必填）】两列为系统自动添加的列，请勿删除或修改。" +
                    " \n 2. 【分配人域账号】填写分配人员的域账号，表示这一行的内容由分配人填写，此列必填。" +
                    " \n 3. 【行标题（非必填）】填写这一行的描述、备注等，便于分配人员了解这一行填写规则，此列选填，可以空着。" +
                    " \n 4. 用户只需填写灰色背景的单元格，其他单元格由分配人填写。" +
                    " \n 5. 在sheet页【示例】中有示例数据，请参考示例填写。";
        } else {
            content = "注意事项：\n 1.【行标题（非必填）】为系统自动添加的列，请勿删除或修改。" +
                    " \n 2. 【行标题（非必填）】填写这一行的描述、备注等，便于分配人员了解这一行填写规则，此列选填，可以空着。" +
                    " \n 3. 用户在灰色背景单元格中填写分配人的域账号，表示这个单元格由分配人填写。" +
                    " \n 4. 在sheet页【示例】中有示例数据，请参考示例填写。";
        }
        return content;
    }

    private void createExampleSheet(XSSFWorkbook workbook, Integer type, int shiftColumnNumber) {
        Sheet sheet = workbook.createSheet("示例");
        Row firstRow = sheet.createRow(0);
        addNewColumn(type, firstRow, sheet);
        IntStream.range(0, 4).forEach(index -> {
            if (index < 2) {
                setCell(firstRow, sheet, shiftColumnNumber + index, "自定义列名" + (index + 1));
            } else if (index == 2) {
                setCell(firstRow, sheet, shiftColumnNumber + index, "自定义列名...");
            } else {
                setCell(firstRow, sheet, shiftColumnNumber + index, "自定义列名n");
            }

            if (type == 1) {
                setRowTemplateExample(shiftColumnNumber, index, sheet);
            } else {
                setCellTemplateExample(shiftColumnNumber, index, sheet);
            }
        });
    }

    private void setCellTemplateExample(int shiftColumnNumber, int index, Sheet sheet) {
        int rowNum = index + 1;
        Row row = sheet.createRow(rowNum);
        IntStream.range(0, shiftColumnNumber + 4).forEach(cellIndex -> {
            Cell cell = row.createCell(cellIndex);
            XSSFCellStyle style = (XSSFCellStyle) sheet.getWorkbook().createCellStyle();
            setForegroundColor(style);
            setBorder(style);
            cell.setCellStyle(style);
            if (cellIndex == 0) {
                return;
            }
            if (rowNum >= 1 && rowNum < 3) {
                if (cellIndex >= 1 && cellIndex < 3) {
                    cell.setCellValue("test1");
                } else {
                    cell.setCellValue("test2");
                }
            } else {
                if (cellIndex >= 1 && cellIndex < 3) {
                    cell.setCellValue("test3");
                } else {
                    cell.setCellValue("test4");
                }
            }
        });
    }

    private void setRowTemplateExample(int shiftColumnNumber, int index, Sheet sheet) {
        int rowNum = index + 1;
        Row row = sheet.createRow(rowNum);
        IntStream.range(0, shiftColumnNumber).forEach(cellIndex -> {
            Cell cell = row.createCell(cellIndex);
            XSSFCellStyle style = (XSSFCellStyle) sheet.getWorkbook().createCellStyle();
            if (cellIndex <= shiftColumnNumber - 1) {
                setForegroundColor(style);
            }
            setBorder(style);
            cell.setCellStyle(style);
            if (cellIndex == 0) {
                cell.setCellValue("test" + rowNum);
            }
        });
    }

    private void addNewColumn(Integer type, Row firstRow, Sheet sheet) {
        if (type == 1) {
            setCell(firstRow, sheet, 0, Constants.ASSIGNER);
            setCell(firstRow, sheet, 1, Constants.EXCEL_TITLE);
        } else {
            setCell(firstRow, sheet, 0, Constants.EXCEL_TITLE);
        }
    }

    private void setCell(Row firstRow, Sheet sheet, int columnIndex, String cellValue) {
        Cell cell = firstRow.createCell(columnIndex, CellType.STRING);
        cell.setCellValue(cellValue);
        sheet.setColumnWidth(columnIndex, 18 * 256);
        setTitleStyle(sheet, cell, false);
    }

    private void setTitleWidth(int columnIndex, Sheet sheet) {
        sheet.autoSizeColumn(columnIndex);
        sheet.setColumnWidth(columnIndex, sheet.getColumnWidth(columnIndex) * 17 / 10);
    }

    private void setTitleData(Sheet sheet, List<LedgerColumnEntity> titleList, Map<Long, Integer> titleIndexMap) {
        Row row = sheet.createRow(0);
        AtomicInteger titleIndex = new AtomicInteger(0);
        // 列下标-列名
        titleList.forEach(column -> {
            int indexValue = titleIndex.getAndIncrement();
            Cell cell = row.createCell(indexValue);
            String cellValue = column.getName();
            cell.setCellValue(cellValue);
            titleIndexMap.put(column.getId(), indexValue);
            setTitleStyle(sheet, cell, true);
            if (StringUtils.isNotBlank(cellValue) && cellValue.length() > 5) {
                setTitleWidth(indexValue, sheet);
            }
        });
    }

    private void setSheetProperties(Sheet sheet) {
        sheet.setDefaultColumnWidth(15);
        sheet.setDefaultRowHeight((short) (20 * 25));

        // 设置工作表保护
        sheet.protectSheet(fileConfig.getExcelPassword());
    }

    private void setRowData(List<LedgerRowEntity> rows, Sheet sheet, Map<Long, Integer> titleIndexMap,
                            LedgerEntity ledgerEntity, boolean isArchive) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        boolean editorFlag = !StringUtils.equals(userHelper.getUser().getUsername(), ledgerEntity.getCreateBy());
        Integer tempType = ledgerEntity.getType();
        Map<Long, List<LedgerRowEntity>> rowsMap = rows.stream().collect(Collectors.groupingBy(LedgerRowEntity::getRowId));
        AtomicInteger rowCellIndex = new AtomicInteger(1);
        List<LedgerNewRowEntity> newRowList = new ArrayList<>();
        String username = userHelper.getUser().getName();
        rowsMap.keySet().stream().sorted().forEach(key -> {
            int index = rowCellIndex.getAndIncrement();
            Row ledgerRow = sheet.createRow(index);
            List<LedgerRowEntity> cellList = rowsMap.get(key);

            cellList.forEach(item -> {
                // 获取列下标
                Long columnId = item.getColumnId();
                int indexValue = titleIndexMap.get(columnId);
                Cell cell = ledgerRow.createCell(indexValue);
                cell.setCellValue(item.getContent());
                if (isArchive) {
                    cell.setCellStyle(getRowLockCellStyle(sheet));
                    return;
                }
                if (editorFlag) {
                    // 记录缩表后的坐标
                    newRowList.add(buildNewRowEntity(ledgerEntity.getTempId(), columnId, key, (long) index));
                }
                // 只有编辑者 并且 编辑者有当前单元格的编辑权限 才行
                if (editorFlag && Objects.equals(item.getIsEdit(), 1) && StringUtils.equals(item.getAssigner(), username)) {
                    cell.setCellStyle(getRowUnlockCellStyle(sheet));
                } else {
                    cell.setCellStyle(getRowLockCellStyle(sheet));
                }
            });

            // 行模板自动填充单元格
            fillCell(sheet, titleIndexMap, tempType, ledgerRow, isArchive);
        });

        if (CollectionUtils.isNotEmpty(newRowList)) {
            newRowMapper.delete(Wrappers.<LedgerNewRowEntity>lambdaQuery().eq(
                    LedgerNewRowEntity::getTempId, ledgerEntity.getTempId()).eq(LedgerNewRowEntity::getAssigner, username));
            newRowMapper.insert(newRowList);
        }
    }

    private void fillCell(Sheet sheet, Map<Long, Integer> titleIndexMap, Integer tempType, Row ledgerRow, boolean isArchive) {
        if (!Objects.equals(tempType, LedgerTypeEnum.ROW_TEMPLATE.getType())) {
            return;
        }

        IntStream.range(2, titleIndexMap.size()).forEach(index -> {
            Cell hasCell = ledgerRow.getCell(index);
            if (Objects.isNull(hasCell)) {
                hasCell = ledgerRow.createCell(index);
                hasCell.setCellValue("");
            }
            if (isArchive) {
                hasCell.setCellStyle(getRowLockCellStyle(sheet));
            } else {
                hasCell.setCellStyle(getRowUnlockCellStyle(sheet));
            }
        });
    }

    private LedgerNewRowEntity buildNewRowEntity(String tempId, Long columnId, Long oldRowId, Long newRowId) {
        LedgerNewRowEntity newRow = new LedgerNewRowEntity();
        newRow.setAssigner(userHelper.getUser().getUsername());
        newRow.setTempId(tempId);
        newRow.setColumnId(columnId);
        newRow.setOldRowId(oldRowId);
        newRow.setNewRowId(newRowId);
        return newRow;
    }

    private String getFileName(String tempName, String username, String fileType, boolean isCreator) {
        if (isCreator) {
            return tempName;
        }
        return String.join("_", FileUtil.mainName(tempName), username, DateUtil.today().replace("-","")) + fileType;
    }

    private String getFilePath(String username, String fileName, boolean isArchive) {
        if (isArchive) {
            return String.join("/", fileConfig.getBasePath(), "ledger", username, DateUtil.today(), "") + fileName;
        }
        return String.join("/", fileConfig.getTmpPath(), username, DateUtil.today(), "") + fileName;
    }

    private void setTitleStyle(Sheet sheet, Cell cell, boolean isLock) {
        CellStyle style = sheet.getWorkbook().createCellStyle();
        if (isLock) {
            // 设置单元格保护
            style.setLocked(true);
        }
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        // 设置居中
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        DataFormat format = sheet.getWorkbook().createDataFormat();
        style.setDataFormat(format.getFormat("@"));
        cell.setCellStyle(style);
    }

    private CellStyle getRowLockCellStyle(Sheet sheet) {
        CellStyle lockStyle = getCellStyle(sheet);
        lockStyle.setLocked(true);
        return lockStyle;
    }

    private CellStyle getRowUnlockCellStyle(Sheet sheet) {
        XSSFCellStyle unlockStyle = (XSSFCellStyle) getCellStyle(sheet);
        unlockStyle.setLocked(false);
        // 设置背景颜色
        setForegroundColor(unlockStyle);
        // 设置边框
        setBorder(unlockStyle);
        return unlockStyle;
    }

    private void setBorder(XSSFCellStyle unlockStyle) {
        unlockStyle.setBorderBottom(BorderStyle.THIN);
        unlockStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        unlockStyle.setBorderLeft(BorderStyle.THIN);
        unlockStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        unlockStyle.setBorderRight(BorderStyle.THIN);
        unlockStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        unlockStyle.setBorderTop(BorderStyle.THIN);
        unlockStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
    }

    private void setForegroundColor(XSSFCellStyle unlockStyle) {
        unlockStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        unlockStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    }

    private CellStyle getCellStyle(Sheet sheet) {
        CellStyle style = sheet.getWorkbook().createCellStyle();
        style.setWrapText(true);
        DataFormat format = sheet.getWorkbook().createDataFormat();
        style.setDataFormat(format.getFormat("@"));
        return style;
    }

    private void saveWorkbook(String filePath, Workbook workbook) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        } catch (IOException e) {
            LOGGER.error(e);
            throw new ApplicationException("台账数据写入失败，请联系管理员");
        }
    }
}
