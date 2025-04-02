package com.digital.doc.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.doc.helper.UserHelper;
import com.digital.doc.mapper.DocHistoryMapper;
import com.digital.doc.service.IDocHistoryService;
import com.digital.model.doc.dto.DocHistoryDto;
import com.digital.model.doc.entity.DocHistoryEntity;
import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 搜索历史内容
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DocHistoryServiceImpl extends ServiceImpl<DocHistoryMapper, DocHistoryEntity> implements IDocHistoryService {
	private static final Logger LOGGER = LogManager.getLogger(DocHistoryServiceImpl.class);

	@Autowired
	private UserHelper userHelper;

	@Autowired
	private DocHistoryMapper docHistoryMapper;

	@Override
	public int createHistory(DocHistoryDto docHistoryDto) {
		// 验证输入参数
		if (StringUtils.isNullOrEmpty(docHistoryDto.getContent())) {
			LOGGER.warn("Missing required parameters: content");
			return 0;
		}

		Long userId = userHelper.getUserId();
		DocHistoryEntity historyEntity = buildHistoryEntity(docHistoryDto);

		// 检查是否存在相同的历史记录
		int existingHistoryCount = docHistoryMapper.findHistoryByContentAndUserId(historyEntity, userId);
		if (existingHistoryCount > 0) {
			LOGGER.info("History already exists for userId:{}, content:{}", userId, historyEntity.getContent());
			return 0;
		}

		historyEntity.setUserId(userId);
		// 插入新的历史记录
		int insertResult = baseMapper.insert(historyEntity);
		if (insertResult > 0) {
			LOGGER.info("History created for userId:{}, content:{}", userId, historyEntity.getContent());
		} else {
			LOGGER.warn("Failed to create history for userId:{}, content:{}", userId, historyEntity.getContent());
		}

		return insertResult;
	}

	@Override
	public Page<DocHistoryEntity> findHistoryList(DocHistoryDto docHistoryDto, Page<DocHistoryEntity> page) {
		return docHistoryMapper.findHistoryList(page, buildHistoryEntity(docHistoryDto), userHelper.getUserId());
	}

	private DocHistoryEntity buildHistoryEntity(DocHistoryDto docHistoryDto) {
		DocHistoryEntity fileEntity = new DocHistoryEntity();
		fileEntity.setContent(docHistoryDto.getContent());
		return fileEntity;
	}

}
