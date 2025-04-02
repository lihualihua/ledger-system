/*
 *    Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the digital.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: lengleng (wangiegie@gmail.com)
 */

package com.digital.doc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.digital.model.doc.dto.DocHistoryDto;
import com.digital.model.doc.entity.DocHistoryEntity;

/**
 * 搜索历史内容
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
public interface IDocHistoryService extends IService<DocHistoryEntity> {
	int createHistory(DocHistoryDto folderDto);

	Page<DocHistoryEntity> findHistoryList(DocHistoryDto docHistoryDto, Page<DocHistoryEntity> page);
}
