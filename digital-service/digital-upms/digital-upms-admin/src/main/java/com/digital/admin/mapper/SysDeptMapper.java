/*
 *
 *      Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the digital.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: lengleng (wangiegie@gmail.com)
 *
 */

package com.digital.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digital.model.admin.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 部门管理 Mapper 接口
 * </p>
 *
 * @author lengleng
 * @since 2018-01-20
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    SysDept findSysDeptByUserId(@Param("userId") Long userId);

    List<SysDept> listSysDept(@Param("userId") Long userId);

    int updateUserDeptByUserId(@Param("deptId") Long deptId, @Param("userId") Long userId);
}
