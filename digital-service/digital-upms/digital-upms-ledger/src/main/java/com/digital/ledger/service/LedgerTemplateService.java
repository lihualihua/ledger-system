package com.digital.ledger.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.digital.model.ledger.dto.LedgerTemplateDto;
import com.digital.model.ledger.entity.LedgerTemplateEntity;
import com.digital.model.ledger.response.LedgerTemplateListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LedgerTemplateService extends IService<LedgerTemplateEntity> {

    void checkSysTemplateCount();

    void readTemplateExcel(MultipartFile file, Integer systemFlag);

    void removeTemplateById(Long id);

    List<LedgerTemplateListVO> findSysTemplateList();

    IPage<LedgerTemplateListVO> findMyTemplates(LedgerTemplateDto templateDto, PageDTO<LedgerTemplateListVO> page);

}
