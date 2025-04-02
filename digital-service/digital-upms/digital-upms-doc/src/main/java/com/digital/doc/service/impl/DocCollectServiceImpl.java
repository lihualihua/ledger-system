package com.digital.doc.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.common.core.exception.CheckedException;
import com.digital.doc.helper.UserHelper;
import com.digital.doc.mapper.DocCollectMapper;
import com.digital.doc.service.IDocCollectService;
import com.digital.doc.service.IDocFileService;
import com.digital.model.doc.entity.DocCollectEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 文件管理
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DocCollectServiceImpl extends ServiceImpl<DocCollectMapper, DocCollectEntity> implements IDocCollectService {
	private static final Logger LOGGER = LogManager.getLogger(DocCollectServiceImpl.class);

	@Autowired
	private IDocFileService docFileService;

	@Autowired
	private UserHelper userHelper;

	@Override
	public void collect(Long fileId) {
		DocCollectEntity collectEntity = buildCollectEntity(fileId);
		DocCollectEntity result = baseMapper.selectOne(Wrappers.<DocCollectEntity>lambdaQuery().eq(DocCollectEntity::getFileId, collectEntity.getFileId()).
				eq(DocCollectEntity::getUserId, collectEntity.getUserId()));
		if (Objects.nonNull(result)) {
			throw new CheckedException("该文件已被收藏，请勿重复收藏!");
		}
		baseMapper.insert(collectEntity);
		docFileService.processCollectCount(fileId, true);
	}

	@Override
	public void removeByFileId(Long fileId) {
		baseMapper.delete(Wrappers.<DocCollectEntity>lambdaQuery().eq(DocCollectEntity::getFileId, fileId).
				eq(DocCollectEntity::getUserId, userHelper.getUserId()));
		docFileService.processCollectCount(fileId, false);
	}

	@Override
	public void removeByCollectId(Long collectId) {
		DocCollectEntity collectEntity = baseMapper.selectOne(Wrappers.<DocCollectEntity>lambdaQuery().eq(DocCollectEntity::getId, collectId));
		baseMapper.delete(Wrappers.<DocCollectEntity>lambdaQuery().eq(DocCollectEntity::getId, collectId));
		docFileService.processCollectCount(collectEntity.getFileId(), false);
	}

	private DocCollectEntity buildCollectEntity(Long fileId) {
		DocCollectEntity docCollectEntity = new DocCollectEntity();
		docCollectEntity.setFileId(fileId);
		docCollectEntity.setUserId(userHelper.getUserId());
		docCollectEntity.setStatus(1);
		return docCollectEntity;
	}

}
