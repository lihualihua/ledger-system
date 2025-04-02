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
package com.digital.admin.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.model.admin.entity.SysDict;
import com.digital.model.admin.entity.SysDictItem;
import com.digital.admin.mapper.SysDictItemMapper;
import com.digital.admin.service.SysDictItemService;
import com.digital.admin.service.SysDictService;
import com.digital.common.core.constant.CacheConstants;
import com.digital.common.core.constant.enums.DictTypeEnum;
import com.digital.common.core.exception.ErrorCodes;
import com.digital.common.core.util.MsgUtils;
import com.digital.common.core.util.R;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 字典项
 *
 * @author lengleng
 * @date 2019/03/19
 */
@Service
@AllArgsConstructor
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements SysDictItemService {

	private final SysDictService dictService;

	/**
	 * 删除字典项
	 * @param id 字典项ID
	 * @return
	 */
	@Override
	@CacheEvict(value = CacheConstants.DICT_DETAILS, allEntries = true)
	public R removeDictItem(Long id) {
		// 根据ID查询字典ID
		SysDictItem dictItem = this.getById(id);
		SysDict dict = dictService.getById(dictItem.getDictId());
		// 系统内置
		if (DictTypeEnum.SYSTEM.getType() == dict.getSystemFlag()) {
			return R.failed(MsgUtils.getMessage(ErrorCodes.SYS_DICT_DELETE_SYSTEM));
		}
		return R.ok(this.removeById(id), "删除成功！");
	}

	/**
	 * 更新字典项
	 * @param item 字典项
	 * @return
	 */
	@Override
	@CacheEvict(value = CacheConstants.DICT_DETAILS, key = "#item.dictType")
	public R updateDictItem(SysDictItem item) {
		// 查询字典
		SysDict dict = dictService.getById(item.getDictId());
		// 系统内置
		if (DictTypeEnum.SYSTEM.getType()== dict.getSystemFlag()) {
			return R.failed(MsgUtils.getMessage(ErrorCodes.SYS_DICT_UPDATE_SYSTEM));
		}
		item.setUpdateTime(DateUtil.toLocalDateTime(new Date()));
		return R.ok(this.updateById(item), "更新成功！");
	}

}
