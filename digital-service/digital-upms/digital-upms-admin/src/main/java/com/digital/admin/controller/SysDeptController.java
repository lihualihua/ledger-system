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

package com.digital.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digital.model.admin.entity.SysDept;
import com.digital.model.admin.vo.DeptExcelVO;
import com.digital.admin.service.SysDeptService;
import com.digital.common.core.util.R;
import com.digital.common.log.annotation.SysLog;
import com.pig4cloud.plugin.excel.annotation.RequestExcel;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 部门管理 前端控制器
 * </p>
 *
 * @author lengleng
 * @since 2018-01-20
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dept")
@Tag(description = "dept", name = "部门管理模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class SysDeptController {

	private final SysDeptService sysDeptService;

	/**
	 * 通过ID查询
	 * @param id ID
	 * @return SysDept
	 */
	@GetMapping("/{id}")
	public R getById(@PathVariable Long id) {
		return R.ok(sysDeptService.getById(id));
	}

	/**
	 * 查询全部部门
	 */
	@GetMapping("/list")
	public R list() {
		return R.ok(sysDeptService.list());
	}

	/**
	 * 分页查找部门
	 *
	 * @param page
	 * @return
	 */
	@GetMapping("/list/{current}/{size}")
	public R list(@ModelAttribute("page") Page<SysDept> page, @RequestParam(required = false) String deptName) {
		if (StringUtils.isBlank(deptName)) {
			return R.ok(sysDeptService.page(page));
		}
		return R.ok(sysDeptService.page(page, Wrappers.<SysDept>lambdaQuery().like(SysDept::getName, deptName)));
	}

	/**
	 * 返回树形菜单集合
	 * @param deptName 部门名称
	 * @return 树形菜单
	 */
	@GetMapping(value = "/tree")
	public R getTree(String deptName) {
		return R.ok(sysDeptService.selectTree(deptName));
	}

	/**
	 * 添加
	 * @param sysDept 实体
	 * @return success/false
	 */
	@SysLog("添加部门")
	@PostMapping("/add")
	@PreAuthorize("@pms.hasPermission('sys_dept_add')")
	public R save(@Valid @RequestBody SysDept sysDept) {
		return R.ok(sysDeptService.save(sysDept));
	}

	/**
	 * 删除
	 * @param id ID
	 * @return success/false
	 */
	@SysLog("删除部门")
	@DeleteMapping("/delete/{id}")
	@PreAuthorize("@pms.hasPermission('sys_dept_del')")
	public R removeById(@PathVariable Long id) {
		return R.ok(sysDeptService.removeDeptById(id));
	}

	/**
	 * 编辑
	 * @param sysDept 实体
	 * @return success/false
	 */
	@SysLog("编辑部门")
	@PutMapping("/update")
	@PreAuthorize("@pms.hasPermission('sys_dept_edit')")
	public R update(@RequestBody SysDept sysDept) {
		sysDept.setUpdateTime(LocalDateTime.now());
		return R.ok(sysDeptService.updateById(sysDept));
	}

	/**
	 * 查收子级列表
	 * @return 返回子级
	 */
	@GetMapping(value = "/getDescendantList/{deptId}")
	public R getDescendantList(@PathVariable Long deptId) {
		return R.ok(sysDeptService.listDescendant(deptId));
	}

	/**
	 * 导出部门
	 * @return
	 */
	@ResponseExcel
	@GetMapping("/export")
	public List<DeptExcelVO> export() {
		return sysDeptService.listExcelVo();
	}

	/**
	 * 导入部门
	 * @param excelVOList
	 * @param bindingResult
	 * @return
	 */
	@PostMapping("import")
	public R importDept(@RequestExcel List<DeptExcelVO> excelVOList, BindingResult bindingResult) {

		return sysDeptService.importDept(excelVOList, bindingResult);
	}

	/**
	 * 根据用户id查询部门
	 */
	@GetMapping("/getDeptByUserId/{userId}")
	public R getDeptByUserId(@Valid @PathVariable("userId") Long userId) {
		return R.ok(sysDeptService.listSysDept(userId));
	}

	/**
	 * 管理员修改用户部门信息
	 *
	 * @param deptId 部门ID
	 * @param userId 用户ID
	 * @return R
	 */
	@PreAuthorize("@pms.hasPermission('sys_dict_edit')")
	@PatchMapping("/updateUserDeptByUserId/{deptId}/{userId}")
	public R updateUserDeptByUserId(@Valid @PathVariable("deptId") Long deptId, @Valid @PathVariable("userId") Long userId) {
		return R.ok(sysDeptService.updateUserDeptByUserId(deptId, userId));
	}
}
