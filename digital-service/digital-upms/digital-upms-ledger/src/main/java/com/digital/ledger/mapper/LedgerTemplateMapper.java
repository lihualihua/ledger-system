package com.digital.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digital.model.ledger.dto.LedgerTemplateDto;
import com.digital.model.ledger.entity.LedgerTemplateEntity;
import com.digital.model.ledger.response.LedgerTemplateListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LedgerTemplateMapper extends BaseMapper<LedgerTemplateEntity> {

    List<LedgerTemplateListVO> findSysTemplateList();

    Page<LedgerTemplateListVO> findMyTemplates(Page<LedgerTemplateListVO> page, @Param("templateDto") LedgerTemplateDto templateDto, @Param("userName") String userName);
}
