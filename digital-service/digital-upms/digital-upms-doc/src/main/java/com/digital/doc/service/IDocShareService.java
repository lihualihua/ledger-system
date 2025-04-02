package com.digital.doc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.digital.model.doc.dto.DocShareDto;
import com.digital.model.doc.entity.DocFileEntity;
import com.digital.model.doc.entity.DocShareEntity;

import java.util.List;

/**
 * 分享
 *
 */
public interface IDocShareService extends IService<DocShareEntity> {

	DocShareEntity saveShare(DocShareDto docShareDto, String tempContextUrl);

	int deleteShare(Long shareId);

	DocShareEntity findByMd5Value(String md5Value);

	List<DocFileEntity> findFilesByMd5Value(DocShareDto shareDto);

	void deleteExpiredShare();

	IPage<DocShareEntity> findMyShare(DocShareDto shareDto, PageDTO<DocShareEntity> page);

	IPage<DocShareEntity> findShareWithMe(DocShareDto shareDto, PageDTO<DocShareEntity> page);

	void deleteShareByFileIds(List<Long> fileIds);

	DocShareEntity findShareDetail(Long shareId);
}
