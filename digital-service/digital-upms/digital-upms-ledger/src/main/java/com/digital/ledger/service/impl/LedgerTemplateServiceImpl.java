package com.digital.ledger.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.admin.mapper.SysDictItemMapper;
import com.digital.common.core.constant.CommonConstants;
import com.digital.common.core.exception.ApplicationException;
import com.digital.common.core.exception.CheckedException;
import com.digital.doc.helper.UserHelper;
import com.digital.ledger.mapper.LedgerTemplateMapper;
import com.digital.ledger.service.LedgerTemplateService;
import com.digital.model.ledger.dto.LedgerTemplateDto;
import com.digital.model.ledger.entity.LedgerTemplateEntity;
import com.digital.model.ledger.response.LedgerTemplateListVO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = Exception.class)
public class LedgerTemplateServiceImpl extends ServiceImpl<LedgerTemplateMapper, LedgerTemplateEntity> implements LedgerTemplateService {
    private static final Logger LOGGER = LogManager.getLogger(LedgerTemplateServiceImpl.class);

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private SysDictItemMapper sysDictItemMapper;

    @Override
    public void checkSysTemplateCount() {
        userHelper.checkSuperAdmin();
        LambdaQueryWrapper<LedgerTemplateEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(LedgerTemplateEntity::getSystemFlag, 1);
        Long count =  baseMapper.selectCount(lambdaQueryWrapper);
        if (count >= 2) {
            throw new CheckedException("最多可上传2个系统模板!");
        }
    }

    /**
     * 导入模板库
     *
     * @param file 文件
     * @param systemFlag 是否系统模版
     */
    @Override
    public void readTemplateExcel(MultipartFile file, Integer systemFlag) {
        String fileName = file.getOriginalFilename();
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 模板id
        String uuidFileName = IdUtil.fastSimpleUUID() + "." + fileType;
        // 模板存放路径
        String filePath = String.join("/", sysDictItemMapper.findItemValueByType(CommonConstants.TEMPLATE_FILE_PATH), DateUtil.today(), uuidFileName);
        // 构建文件路径
        try {
            // 将文件内容保存到目标位置
            FileUtil.writeBytes(file.getBytes(), filePath);
        } catch (IOException e) {
            LOGGER.error(e);
            throw new ApplicationException("台账文件存储失败!");
        }
         insertLedgerTemplate(fileName, filePath, systemFlag);
    }

    // 保存模板
    private void insertLedgerTemplate(String fileName, String templatePath, Integer systemFlag) {
        LedgerTemplateEntity templateEntity = new LedgerTemplateEntity();
        templateEntity.setName(fileName);
        templateEntity.setPath(templatePath);
        templateEntity.setSystemFlag(systemFlag);
        baseMapper.insert(templateEntity);
    }

    @Override
    public void removeTemplateById(Long id) {
        LedgerTemplateEntity templateEntity = baseMapper.selectById(id);
        if (Objects.nonNull(templateEntity)) {
            if (!templateEntity.getCreateBy().equals(userHelper.getUser().getName())) {
                throw new CheckedException("仅限删除个人创建模板！");
            }
        }else {
            LOGGER.error("id:{}", id);
            throw new CheckedException("id："+ id +"不存在!");
        }
        baseMapper.deleteById(id);
    }

    @Override
    public List<LedgerTemplateListVO> findSysTemplateList() {
        return baseMapper.findSysTemplateList();
    }

    @Override
    public IPage<LedgerTemplateListVO> findMyTemplates(LedgerTemplateDto templateDto, PageDTO<LedgerTemplateListVO> page) {
        return baseMapper.findMyTemplates(page, templateDto, userHelper.getUser().getName());
    }

}
