package com.digital.ledger.service;

import org.springframework.web.multipart.MultipartFile;

public interface IImportLedgerService {
    void readExcel(MultipartFile file, Long templateId);

    void editorUpload(MultipartFile file, String tempId);
}
