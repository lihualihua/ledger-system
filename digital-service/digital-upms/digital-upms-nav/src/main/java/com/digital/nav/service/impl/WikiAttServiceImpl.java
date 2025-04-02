package com.digital.nav.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.common.core.exception.ApplicationException;
import com.digital.doc.service.IDocFileService;
import com.digital.doc.service.IDocQueryService;
import com.digital.model.doc.dto.DocFileDto;
import com.digital.model.doc.entity.DocFileEntity;
import com.digital.model.nav.dto.WikiAttDto;
import com.digital.model.nav.dto.WikiDto;
import com.digital.model.nav.entity.WikiAttEntity;
import com.digital.nav.mapper.WikiAttMapper;
import com.digital.nav.service.IWikiAttService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class WikiAttServiceImpl extends ServiceImpl<WikiAttMapper, WikiAttEntity> implements IWikiAttService {

    @Autowired
    private IDocFileService docFileService;

    @Autowired
    private IDocQueryService queryService;

    @Override
    public Long initAttachmentDir(Long parentId, WikiDto wiki) {
        DocFileEntity fileEntity = queryService.findDelById(parentId, false);
        if (Objects.isNull(fileEntity)) {
            throw new ApplicationException("该目录不存在，请联系管理员!");
        }
        Map<String, String> map = new HashMap<>();
        DocFileDto wikiFileDto = new DocFileDto();
        wikiFileDto.setFileName(wiki.getTitle());
        wikiFileDto.setDelFlag(1);
        wikiFileDto.setFolderFlag(1);
        wikiFileDto.setParentId(fileEntity.getFileId());
        wikiFileDto.setOwnership(1);
        wikiFileDto.setIcon("icon-file-fill-24");
        DocFileEntity wikiFileEntity = queryService.findDocFileEntity(wikiFileDto);
        if (Objects.isNull(wikiFileEntity)) {
            wikiFileEntity = new DocFileEntity();
            BeanUtil.copyProperties(wikiFileDto, wikiFileEntity);
            docFileService.createFile(wikiFileEntity, null);
            return createVersionDir(wikiFileEntity.getFileId(), wiki).getFileId();
        }
        wikiFileDto.setParentId(wikiFileEntity.getFileId());
        wikiFileDto.setFileName(wiki.getVersion());
        wikiFileEntity = queryService.findDocFileEntity(wikiFileDto);
        if (Objects.isNull(wikiFileEntity)) {
            return createVersionDir(wikiFileDto.getParentId(), wiki).getFileId();
        }
        return wikiFileEntity.getFileId();
    }

    @Override
    public void saveWikiAtt(WikiAttDto wikiAttDto) {
        List<Long> fileIds = wikiAttDto.getFileIds();
        if (CollectionUtils.isEmpty(fileIds)) {
            return;
        }
        Long wikiId = wikiAttDto.getWikiId();
        List<WikiAttEntity> attList = new ArrayList<>();
        fileIds.forEach(fileId -> {
            WikiAttEntity attEntity = new WikiAttEntity();
            attEntity.setFileId(fileId);
            DocFileEntity fileEntity = queryService.findById(fileId);
            if (Objects.isNull(fileEntity)) {
                return;
            }
            attEntity.setFileName(fileEntity.getFileName());
            attEntity.setWikiId(wikiId);
            attList.add(attEntity);
        });
        baseMapper.insert(attList);
    }

    @Override
    public void deleteWikiAtt(WikiAttDto wikiAttDto) {
        List<Long> fileIds = wikiAttDto.getFileIds();
        if (CollectionUtils.isEmpty(fileIds)) {
            return;
        }
        Long wikiId = wikiAttDto.getWikiId();
        fileIds.forEach(fileId -> {
            baseMapper.delete(Wrappers.<WikiAttEntity>lambdaQuery().eq(
                    WikiAttEntity::getFileId, fileId).eq(WikiAttEntity::getWikiId, wikiId));
        });
        docFileService.deleteFile(fileIds);
    }

    @Override
    public void deleteByWikiId(Long wikiId) {
        List<WikiAttEntity> attEntityList = findByWikiId(wikiId);
        if (CollectionUtils.isEmpty(attEntityList)) {
            return;
        }
        baseMapper.delete(Wrappers.<WikiAttEntity>lambdaQuery().eq(WikiAttEntity::getWikiId, wikiId));
        List<Long> fileIds = attEntityList.stream().map(WikiAttEntity::getFileId).collect(Collectors.toList());
        docFileService.deleteFile(fileIds);
    }

    @Override
    public void deleteWikiDir(String fileName, Long fileId, boolean isParentId) {
        List<Long> fileIds = new ArrayList<>();
        if (isParentId) {
            DocFileDto wikiFileDto = new DocFileDto();
            wikiFileDto.setFileName(fileName);
            wikiFileDto.setDelFlag(1);
            wikiFileDto.setFolderFlag(1);
            wikiFileDto.setParentId(fileId);
            wikiFileDto.setOwnership(1);
            DocFileEntity wikiFileEntity = queryService.findDocFileEntity(wikiFileDto);
            if (Objects.isNull(wikiFileEntity)) {
                return;
            }
            fileIds.add(wikiFileEntity.getFileId());
        } else {
            fileIds.add(fileId);
        }
        docFileService.deleteFile(fileIds);
    }

    @Override
    public List<WikiAttEntity> findByWikiId(Long wikiId) {
        return baseMapper.selectList(Wrappers.<WikiAttEntity>lambdaQuery().eq(WikiAttEntity::getWikiId, wikiId));
    }

    private DocFileEntity createVersionDir(Long wikiFileDto, WikiDto wiki) {
        DocFileEntity versionFileEntity = new DocFileEntity();
        versionFileEntity.setParentId(wikiFileDto);
        versionFileEntity.setOwnership(1);
        versionFileEntity.setFileName(wiki.getVersion());
        versionFileEntity.setDelFlag(1);
        versionFileEntity.setFolderFlag(1);
        versionFileEntity.setIcon("icon-file-fill-24");
        docFileService.createFile(versionFileEntity, null);
        return versionFileEntity;
    }
}
