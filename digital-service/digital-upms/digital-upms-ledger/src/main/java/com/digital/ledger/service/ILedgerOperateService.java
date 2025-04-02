package com.digital.ledger.service;

import com.digital.model.doc.entity.DocFileEntity;
import com.digital.model.ledger.dto.TemplateDto;
import com.digital.model.ledger.entity.LedgerEntity;

import javax.servlet.http.HttpServletResponse;

public interface ILedgerOperateService {
    /**
     * 归档
     *
     * @param ledgerEntity
     */
    void archive(LedgerEntity ledgerEntity, boolean ignoreCheck);

    /**
     * 撤销
     *
     * @param ledgerEntity
     */
    void revoke(LedgerEntity ledgerEntity);

    /**
     * 删除台账
     *
     * @param ledgerEntity
     */
    void delete(LedgerEntity ledgerEntity);

    /**
     * 催办
     *
     * @param ledgerEntity
     */
    void urge(LedgerEntity ledgerEntity);

    /**
     * 生成台账excel
     *
     * @param templateEntity
     * @param fixed
     * @param isArchive
     * @return
     */
    DocFileEntity generateLedgerExcel(LedgerEntity templateEntity, Integer fixed, boolean isArchive);

    /**
     * 下载台账excel
     *
     * @param template
     * @param response
     */
    void downloadLedgerExcel(TemplateDto template, HttpServletResponse response);

    /**
     * 下载台账excel模板
     *
     * @param type
     * @param response
     */
    void downloadLedgerTemplate(Long id, Integer type, HttpServletResponse response);
}
