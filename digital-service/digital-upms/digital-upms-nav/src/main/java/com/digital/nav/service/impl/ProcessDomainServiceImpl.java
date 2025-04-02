package com.digital.nav.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.common.core.exception.CheckedException;
import com.digital.doc.helper.DictHelper;
import com.digital.doc.helper.UserHelper;
import com.digital.doc.service.IDocQueryService;
import com.digital.model.doc.entity.DocFileEntity;
import com.digital.model.doc.vo.DocFileDetailVO;
import com.digital.model.nav.dto.ProcessDomainDto;
import com.digital.model.nav.entity.ProcessDomainEntity;
import com.digital.model.nav.vo.FolderInfoVO;
import com.digital.nav.mapper.ProcessDomainMapper;
import com.digital.nav.service.IProcessDomainService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ProcessDomainServiceImpl extends ServiceImpl<ProcessDomainMapper, ProcessDomainEntity> implements IProcessDomainService {
    private static final Logger LOGGER = LogManager.getLogger(ProcessDomainServiceImpl.class);

    @Autowired
    private DictHelper dictHelper;

    @Autowired
    private IDocQueryService docQueryService;

    @Autowired
    private UserHelper userHelper;

    @Override
    public void create(ProcessDomainDto processDomainDto) {
        userHelper.checkSuperAdmin();
        checkProcessDomainEntity(processDomainDto);
        ProcessDomainEntity entity = buildProcessDomainEntity(processDomainDto);
        entity.setStatus(1);
        baseMapper.insert(entity);
    }

    @Override
    public void update(ProcessDomainDto processDomainDto) {
        userHelper.checkSuperAdmin();
        checkProcessDomainEntity(processDomainDto);
        baseMapper.updateById(buildProcessDomainEntity(processDomainDto));
    }

    private void checkProcessDomainEntity(ProcessDomainDto processDomainDto) {
        ProcessDomainEntity entity = findProcessDomain(processDomainDto);
        if (Objects.nonNull(entity)) {
            if (Objects.equals(entity.getId(), processDomainDto.getId())) {
                return;
            }
            throw new CheckedException("该流程域已存在，请勿重复添加!");
        }
    }

    @Override
    public ProcessDomainEntity findProcessDomain(ProcessDomainDto processDomainDto) {
        return baseMapper.selectOne(Wrappers.<ProcessDomainEntity>lambdaQuery().eq(
                ProcessDomainEntity::getName, processDomainDto.getName()));
    }

    @Override
    public void delete(Long id) {
        userHelper.checkSuperAdmin();
        baseMapper.deleteById(id);
    }

    @Override
    public List<ProcessDomainEntity> findProcessDomainList(ProcessDomainDto processDomainDto) {
        List<ProcessDomainEntity> processDomainEntityList = baseMapper.selectList(Wrappers.<ProcessDomainEntity>lambdaQuery().eq(Objects.nonNull(processDomainDto.getStatus()),
                ProcessDomainEntity::getStatus, processDomainDto.getStatus()).orderByAsc(
                        ProcessDomainEntity::getOrder).orderByDesc(ProcessDomainEntity::getCreateTime));
        if (CollectionUtils.isEmpty(processDomainEntityList)) {
            return Collections.emptyList();
        }
        processDomainEntityList.forEach(item -> {
            try {
                Long fileId = item.getFolderId();
                if (Objects.isNull(fileId)) {
                    return;
                }
                DocFileDetailVO fileDetailInfo = docQueryService.findFileDetailInfo(fileId, true);
                if (Objects.nonNull(fileDetailInfo)) {
                    item.setLocation(fileDetailInfo.getLocation());
                }
            } catch (Exception ex) {
                LOGGER.error(ex);
            }
        });
        return processDomainEntityList;
    }

    @Override
    public List<FolderInfoVO> findStorageDirList() {
        Long folderId = dictHelper.getLongValue("storageDir", "storageDirId", 1878967186875359233L);
        List<DocFileEntity> fileEntityList = docQueryService.findChildFilesByParentId(folderId);
        if (CollectionUtils.isEmpty(fileEntityList)) {
            return Collections.emptyList();
        }

        List<FolderInfoVO> templateEntityList = Lists.newArrayList();
        fileEntityList.forEach(file -> {
            // 过滤文件，只保留文件夹
            if (Objects.equals(file.getFolderFlag(), 0)) {
                return;
            }
            FolderInfoVO folder = new FolderInfoVO();
            folder.setFileId(file.getFileId());
            folder.setFileName(file.getFileName());
            templateEntityList.add(folder);
        });
        return templateEntityList;
    }

    @Override
    public ProcessDomainEntity findById(Long id) {
        return baseMapper.selectById(id);
    }

    private ProcessDomainEntity buildProcessDomainEntity(ProcessDomainDto processDomainDto) {
        ProcessDomainEntity entity = new ProcessDomainEntity();
        entity.setId(processDomainDto.getId());
        entity.setName(processDomainDto.getName());
        entity.setDesc(processDomainDto.getDesc());
        entity.setOrder(processDomainDto.getOrder());
        entity.setStatus(processDomainDto.getStatus());
        entity.setFolderId(processDomainDto.getFolderId());
        return entity;
    }
}
