package com.digital.ledger.util;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.digital.common.core.exception.ApplicationException;
import com.digital.common.core.exception.CheckedException;
import com.digital.doc.helper.UserHelper;
import com.digital.ledger.enums.LedgerStatusEnum;
import com.digital.ledger.helper.LedgerAssignerHelper;
import com.digital.ledger.service.LedgerService;
import com.digital.model.ledger.entity.LedgerAssignerEntity;
import com.digital.model.ledger.entity.LedgerEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Component
public class BaseUtils {

    private static final Logger LOGGER = LogManager.getLogger(BaseUtils.class);

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private LedgerAssignerHelper assignerHelper;

    /**
     * 检查文件类型
     *
     * @param file file
     */
    protected void checkFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            throw new ApplicationException("服务器处理异常, 文件名获取失败!");
        }
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!fileType.equals(Constants.XLSX)) {
            LOGGER.error("该文件类型不支持! 文件类型：{}", fileType);
            throw new CheckedException("该文件类型不支持，请导入.xlsx类型文件");
        }
    }

    /**
     * 查找创建者的台账信息
     *
     * @param tempId
     * @return
     */
    protected LedgerEntity findCreatorLedger(String tempId) {
        LedgerEntity ledgerEntity = ledgerService.getOne(Wrappers.<LedgerEntity>lambdaQuery().eq(LedgerEntity::getTempId,
                tempId).eq(LedgerEntity::getCreateById, userHelper.getUserId()));
        checkLedger(ledgerEntity);
        return ledgerEntity;
    }

    /**
     * 查找编辑者的台账信息
     *
     * @param tempId
     * @return
     */
    protected LedgerEntity findEditorLedger(String tempId) {
        LedgerEntity ledgerEntity = ledgerService.getOne(Wrappers.<LedgerEntity>lambdaQuery().eq(LedgerEntity::getTempId, tempId));
        checkLedger(ledgerEntity);
        LedgerAssignerEntity assignerEntity = assignerHelper.findAssignerEntity(tempId);
        if (Objects.isNull(assignerEntity)) {
            throw new ApplicationException("您无权限编辑该台账!");
        }
        return ledgerEntity;
    }

    private static void checkLedger(LedgerEntity ledgerEntity) {
        if (Objects.isNull(ledgerEntity)) {
            throw new ApplicationException("台账不存在，请联系管理员!");
        }
        if (Objects.equals(ledgerEntity.getStatus(), LedgerStatusEnum.ARCHIVED.getCode())) {
            throw new ApplicationException("台账已归档，不允许操作!");
        }
    }

    /**
     * 检查是否存在样式的空列名
     *
     * @param stringCellValue 列值
     * @param columnIndex 列坐标
     */
    public static void checkColumnIsEmpty(String stringCellValue, Long columnIndex){
        // 检查列名为null或者""
        if (StringUtils.isBlank(stringCellValue)) {
            throw new CheckedException("第 "+ getColumnLetter(columnIndex) +" 列名为空，请重新填写并导入！");
        }
    }

    /**
     * 判断列坐标是否按顺序自增1
     *
     * @param list 列坐标
     */
    public static void isIncreasing(List<Long> list) {
        IntStream.range(1, list.size())
                .filter(i -> list.get(i) - list.get(i - 1) != 1)
                .findAny() // 如果找到不符合条件的元素，则终止流并返回一个Optional对象。
                .ifPresent(i -> { throw new IllegalArgumentException("第 "+ getColumnLetter((long) i) +" 列名为空，请重新填写并导入！"); });
    }

    /**
     * 将列坐标转换位Excel对应列字母,范围（A-Z，总共26列）
     *
     * @param columnIndex 列坐标
     * @return 列字母
     */
    public static String getColumnLetter(Long columnIndex) {
        StringBuilder sb = new StringBuilder();
        while (columnIndex >= 0) {
            long letterIndex = columnIndex % 26;
            sb.insert(0, (char)('A' + letterIndex));
            columnIndex = (columnIndex / 26) - 1;
        }
        return sb.toString();
    }

    /**
     * 将列坐标转换位Excel对应列字母,范围（A-IV，总共256列）
     *
     * @param columnIndex 列坐标
     * @return 列字母
     */
    private static String getSimpleColumnLetter(Long columnIndex) {
        StringBuilder sb = new StringBuilder();
        while (columnIndex >= 0) {
            sb.insert(0, (char)('A' + (columnIndex % 26)));
            columnIndex = columnIndex / 26 - 1;
        }
        return sb.toString();
    }

}
