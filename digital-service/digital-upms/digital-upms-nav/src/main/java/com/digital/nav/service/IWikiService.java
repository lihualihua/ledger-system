package com.digital.nav.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.digital.model.nav.dto.WikiDto;
import com.digital.model.nav.entity.WikiEntity;
import com.digital.model.nav.entity.WikiContentEntity;

import java.util.List;

public interface IWikiService extends IService<WikiEntity> {

    void saveWiki(WikiDto wikiDto);

    void saveWikiContent(WikiDto wikiDto);

    void deleteWiki(Long wikiId);

    WikiEntity findById(Long wikiId);

    WikiEntity findWiki(WikiDto wikiDto);

    WikiContentEntity findWikiContent(WikiDto wikiDto);

    List<WikiEntity> findWikiHistoryList(String wikiCode);

    List<String> findWikiVersionList(String wikiCode, Integer status);

    IPage<WikiEntity> findWikiList(Long domainId, PageDTO page);

    IPage<WikiEntity> findWikiReleaseList(WikiDto wiki, PageDTO page);
}
