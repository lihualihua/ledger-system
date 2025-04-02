package com.digital.nav.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digital.model.nav.dto.WikiAttDto;
import com.digital.model.nav.dto.WikiDto;
import com.digital.model.nav.entity.WikiAttEntity;

import java.util.List;


public interface IWikiAttService extends IService<WikiAttEntity> {

    Long initAttachmentDir(Long parentId, WikiDto wiki);

    void saveWikiAtt(WikiAttDto wikiAttDto);

    void deleteWikiAtt(WikiAttDto wikiAttDto);

    void deleteByWikiId(Long wikiId);

    void deleteWikiDir(String fileName, Long fileId, boolean isParentId);

    List<WikiAttEntity> findByWikiId(Long wikiId);
}
