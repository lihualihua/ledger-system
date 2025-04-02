package com.digital.nav.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.common.core.exception.ApplicationException;
import com.digital.common.core.exception.CheckedException;
import com.digital.doc.helper.UserHelper;
import com.digital.model.nav.dto.WikiDto;
import com.digital.model.nav.entity.ProcessDomainEntity;
import com.digital.model.nav.entity.WikiEntity;
import com.digital.model.nav.entity.WikiContentEntity;
import com.digital.nav.mapper.WikiAttMapper;
import com.digital.nav.mapper.WikiContentMapper;
import com.digital.nav.mapper.WikiMapper;
import com.digital.nav.service.IProcessDomainService;
import com.digital.nav.service.IWikiAttService;
import com.digital.nav.service.IWikiService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class WikiServiceImpl extends ServiceImpl<WikiMapper, WikiEntity> implements IWikiService {

    @Autowired
    private WikiAttMapper wikiAttMapper;

    @Autowired
    private WikiContentMapper wikiContentMapper;

    @Autowired
    private IProcessDomainService processDomainService;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private IWikiAttService wikiAttService;

    @Override
    public void saveWiki(WikiDto wikiDto) {
        userHelper.checkSuperAdmin();
        if (StringUtils.isBlank(wikiDto.getCode()) && StringUtils.isBlank(wikiDto.getTitle())) {
            throw new CheckedException("wiki标题和编码不能同时为空!");
        }
        WikiEntity wikiEntity = buildWikiEntity(wikiDto);
        String wikiCode = wikiEntity.getCode();
        WikiEntity result = baseMapper.selectOne(Wrappers.<WikiEntity>lambdaQuery().eq(
                WikiEntity::getCode, wikiCode).eq(WikiEntity::getVersion, wikiDto.getVersion()));

        if (Objects.nonNull(result)) {
            wikiEntity.setId(result.getId());
            baseMapper.updateById(wikiEntity);
            return;
        }
        if (Objects.isNull(wikiEntity.getDomainId())) {
            throw new CheckedException("领域编号不能为空!");
        }
        ProcessDomainEntity domainEntity = processDomainService.findById(wikiDto.getDomainId());
        if (Objects.isNull(domainEntity)) {
            throw new ApplicationException("获取流程域失败，请联系管理员!");
        }
        Long folderId = wikiAttService.initAttachmentDir(domainEntity.getFolderId(), wikiDto);
        wikiEntity.setFolderId(folderId);
        wikiEntity.setStatus(0);
        wikiEntity.setVersionFlag(1);
        LambdaUpdateWrapper<WikiEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(WikiEntity::getCode, wikiCode);
        wrapper.set(WikiEntity::getVersionFlag, 0);
        baseMapper.update(wrapper);
        baseMapper.insert(wikiEntity);
    }

    @Override
    public void saveWikiContent(WikiDto wikiDto) {
        userHelper.checkSuperAdmin();
        String wikiCode = wikiDto.getCode();
        if (StringUtils.isBlank(wikiCode)) {
            throw new CheckedException("WIKI编码不存在!");
        }
        String version = wikiDto.getVersion();
        if (StringUtils.isBlank(version)) {
            throw new CheckedException("WIKI版本不能为空!");
        }
        WikiContentEntity wikiContentEntity = buildWikiHistoryEntity(wikiDto);
        WikiContentEntity result = wikiContentMapper.selectOne(Wrappers.<WikiContentEntity>lambdaQuery().eq(
                WikiContentEntity::getWikiCode, wikiCode).eq(WikiContentEntity::getVersion, wikiDto.getVersion()));
        if (Objects.isNull(result)) {
            wikiContentMapper.insert(wikiContentEntity);
        } else {
            wikiContentEntity.setId(result.getId());
            wikiContentMapper.updateById(wikiContentEntity);
        }

        WikiEntity wiki = baseMapper.selectOne(Wrappers.<WikiEntity>lambdaQuery().eq(
                WikiEntity::getCode, wikiDto.getCode()).eq(WikiEntity::getVersionFlag, 1));
        if (Objects.isNull(wiki)) {
            return;
        }
        wikiDto.setTitle(wiki.getTitle());
        wikiDto.setDomainId(wiki.getDomainId());
        wikiDto.setOrder(wiki.getOrder());
        wikiDto.setIcon(wiki.getIcon());
        saveWiki(wikiDto);
    }

    private WikiEntity buildWikiEntity(WikiDto wikiDto) {
        WikiEntity wikiEntity = new WikiEntity();
        String title = wikiDto.getTitle();
        Long domainId = wikiDto.getDomainId();
        String code = wikiDto.getCode();
        wikiEntity.setTitle(title);
        wikiEntity.setIcon(wikiDto.getIcon());
        wikiEntity.setOrder(wikiDto.getOrder());
        wikiEntity.setStatus(wikiDto.getStatus());
        wikiEntity.setVersion(wikiDto.getVersion());
        wikiEntity.setCode(getWikiCode(code, title, domainId));
        wikiEntity.setDomainId(domainId);
        wikiEntity.setDesc(wikiDto.getDesc());
        return wikiEntity;
    }

    private WikiContentEntity buildWikiHistoryEntity(WikiDto wikiDto) {
        WikiContentEntity wikiContentEntity = new WikiContentEntity();
        wikiContentEntity.setWikiCode(wikiDto.getCode());
        wikiContentEntity.setContent(wikiDto.getContent());
        wikiContentEntity.setOriginalContent(wikiDto.getOriginalContent());
        wikiContentEntity.setVersion(wikiDto.getVersion());
        return wikiContentEntity;
    }

    private String getWikiCode(String wikiCode, String title, Long domainId) {
        if (StringUtils.isNotBlank(wikiCode)) {
            return wikiCode;
        }
        return DigestUtil.md5Hex16(title + domainId);
    }

    @Override
    public void deleteWiki(Long wikiId) {
        userHelper.checkSuperAdmin();
        WikiEntity wiki = baseMapper.selectOne(Wrappers.<WikiEntity>lambdaQuery().eq(WikiEntity::getId, wikiId));
        if (Objects.isNull(wiki)) {
            return;
        }
        if (Objects.equals(wiki.getStatus(), 1)) {
            throw new CheckedException("该版本已发布，不允许删除，请先下线再删除!");
        }
        List<WikiContentEntity> result = wikiContentMapper.selectList(Wrappers.<WikiContentEntity>lambdaQuery().eq(
                WikiContentEntity::getWikiCode, wiki.getCode()));
        if (CollectionUtils.isNotEmpty(result)) {
            List<Long> historyIds = result.stream().map(WikiContentEntity::getId).collect(Collectors.toList());
            wikiAttMapper.deleteByIds(historyIds);
            wikiContentMapper.deleteByIds(historyIds);
        }

        baseMapper.deleteById(wikiId);

        // 删除wiki附件
        wikiAttService.deleteByWikiId(wikiId);
        wikiAttService.deleteWikiDir(wiki.getVersion(), wiki.getFolderId(), false);
        // 如果删除最新版本，则把最后一个版本维护为最新版本
        if (Objects.equals(wiki.getVersionFlag(), 1)) {
            List<WikiEntity> wikiEntityList = findWikiHistoryList(wiki.getCode());
            if (CollectionUtils.isEmpty(wikiEntityList)) {
                // 如果删除最后一个版本，也删除对应的目录
                ProcessDomainEntity domainEntity = processDomainService.findById(wiki.getDomainId());
                wikiAttService.deleteWikiDir(wiki.getTitle(), domainEntity.getFolderId(), true);
                return;
            }
            WikiEntity lastWiki = wikiEntityList.get(0);
            lastWiki.setVersionFlag(1);
            baseMapper.updateById(lastWiki);
        }
    }

    @Override
    public WikiEntity findById(Long wikiId) {
        return baseMapper.selectOne(Wrappers.<WikiEntity>lambdaQuery().eq(
                WikiEntity::getId, wikiId));
    }

    @Override
    public WikiEntity findWiki(WikiDto wikiDto) {
        WikiEntity wiki = baseMapper.selectOne(Wrappers.<WikiEntity>lambdaQuery().eq(
                WikiEntity::getCode, wikiDto.getCode()).eq(WikiEntity::getVersion, wikiDto.getVersion()));
        if (Objects.isNull(wiki)) {
            return null;
        }
        // 最新版本加载历史版本信息
        if (Objects.equals(wiki.getVersionFlag(), 1)) {
            setChildren(wiki);
        }
        return wiki;
    }

    private void setChildren(WikiEntity wiki) {
        List<WikiEntity> wikiEntityList = findWikiHistoryList(wiki.getCode());
        if (CollectionUtils.isEmpty(wikiEntityList)) {
            return;
        }
        wikiEntityList = wikiEntityList.stream().filter(item -> Objects.equals(item.getVersionFlag(), 0)).collect(Collectors.toList());
        wiki.setChildren(wikiEntityList);
    }

    @Override
    public List<WikiEntity> findWikiHistoryList(String wikiCode) {
        return baseMapper.selectList(Wrappers.<WikiEntity>lambdaQuery().eq(
                WikiEntity::getCode, wikiCode).orderByDesc(WikiEntity::getCreateTime));
    }

    @Override
    public WikiContentEntity findWikiContent(WikiDto wikiDto) {
        String version = wikiDto.getVersion();
        LambdaQueryWrapper<WikiEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WikiEntity::getCode, wikiDto.getCode());
        if (StringUtils.isBlank(version)) {
            queryWrapper.eq(WikiEntity::getStatus, 1);
            queryWrapper.orderByDesc(WikiEntity::getCreateTime).last("limit 1");
        } else {
            queryWrapper.eq(WikiEntity::getVersion, version);
        }
        WikiEntity wiki = baseMapper.selectOne(queryWrapper);
        if (Objects.isNull(wiki)) {
            return null;
        }
        if (Objects.equals(wiki.getStatus(), 0) && !userHelper.isSuperAdmin()) {
            throw new ApplicationException("您无权限查看该版本内容，请联系管理员!");
        }
        WikiContentEntity result = wikiContentMapper.selectOne(Wrappers.<WikiContentEntity>lambdaQuery().eq(
                WikiContentEntity::getWikiCode, wiki.getCode()).eq(WikiContentEntity::getVersion, wiki.getVersion()));
        if (Objects.isNull(result)) {
            result = new WikiContentEntity();
        }
        result.setTitle(wiki.getTitle());
        result.setDesc(wiki.getDesc());
        return result;
    }

    @Override
    public List<String> findWikiVersionList(String wikiCode, Integer status) {
        List<WikiEntity> wikiList = findWikiHistoryList(wikiCode);
        if (CollectionUtils.isEmpty(wikiList)) {
            return Collections.emptyList();
        }
        if (Objects.nonNull(status)) {
            wikiList = wikiList.stream().filter(item -> Objects.equals(item.getStatus(), status)).collect(Collectors.toList());
        }
        return wikiList.stream().map(WikiEntity::getVersion).collect(Collectors.toList());
    }

    @Override
    public IPage<WikiEntity> findWikiList(Long domainId, PageDTO page) {
        LambdaQueryWrapper<WikiEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WikiEntity::getDomainId, domainId);
        queryWrapper.eq(WikiEntity::getVersionFlag, 1);
        queryWrapper.orderByAsc(WikiEntity::getOrder);
        queryWrapper.orderByDesc(WikiEntity::getUpdateTime);
        IPage<WikiEntity> result =  baseMapper.selectPage(page, queryWrapper);
        if (CollectionUtils.isEmpty(result.getRecords())) {
            return result;
        }
        result.getRecords().forEach(this::setChildren);
        return result;
    }

    @Override
    public IPage<WikiEntity> findWikiReleaseList(WikiDto wiki, PageDTO page) {
        IPage<WikiEntity> wikiPage = baseMapper.findWikiReleaseList(page, wiki);
        List<WikiEntity> wikiEntityList = wikiPage.getRecords();
        if (Objects.nonNull(wikiEntityList)) {
            wikiEntityList.forEach(item -> item.setWikiAttList(wikiAttService.findByWikiId(item.getId())));
        }
        return wikiPage;
    }
}
