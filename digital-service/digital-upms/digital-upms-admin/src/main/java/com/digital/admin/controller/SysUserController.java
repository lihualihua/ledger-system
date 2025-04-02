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

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digital.admin.service.SysDeptService;
import com.digital.model.admin.dto.UserDTO;
import com.digital.model.admin.entity.SysDept;
import com.digital.model.admin.entity.SysUser;
import com.digital.model.admin.vo.UserExcelVO;
import com.digital.admin.service.SysUserService;
import com.digital.common.core.constant.CommonConstants;
import com.digital.common.core.exception.ErrorCodes;
import com.digital.common.core.util.MsgUtils;
import com.digital.common.core.util.R;
import com.digital.common.log.annotation.SysLog;
import com.digital.common.security.annotation.Inner;
import com.digital.common.security.util.SecurityUtils;
import com.pig4cloud.plugin.excel.annotation.RequestExcel;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author lengleng
 * @date 2018/12/16
 */
@RestController
@AllArgsConstructor
@RequestMapping("/user")
@Tag(description = "user", name = "用户管理模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class SysUserController {

	private final SysUserService userService;

	@Autowired
	private SysDeptService deptService;

	/**
	 * 获取指定用户全部信息
	 * @return 用户信息
	 */
	@Inner
	@GetMapping(value = { "/info/query" })
	public R info(@RequestParam(required = false) String username, @RequestParam(required = false) String phone) {
		SysUser user = userService.getOne(Wrappers.<SysUser>query()
			.lambda()
			.eq(StrUtil.isNotBlank(username), SysUser::getUsername, username)
			.eq(StrUtil.isNotBlank(phone), SysUser::getPhone, phone));
		if (user == null) {
			return R.failed(MsgUtils.getMessage(ErrorCodes.SYS_USER_USERINFO_EMPTY, username));
		}
		return R.ok(userService.findUserInfo(user));
	}

	/**
	 * 获取当前用户全部信息
	 * @return 用户信息
	 */
	@GetMapping(value = { "/info" })
	public R info() {
		String username = SecurityUtils.getUser().getUsername();
		SysUser user = userService.getOne(Wrappers.<SysUser>query().lambda().eq(SysUser::getUsername, username));
		if (user == null) {
			return R.failed(MsgUtils.getMessage(ErrorCodes.SYS_USER_QUERY_ERROR));
		}
		return R.ok(userService.findUserInfo(user));
	}

	/**
	 * 通过ID查询用户信息
	 * @param id ID
	 * @return 用户信息
	 */
	@GetMapping("/details/{id}")
	public R user(@PathVariable Long id) {
		return R.ok(userService.selectUserVoById(id));
	}

	/**
	 * 查询用户信息
	 * @param query 查询条件
	 * @return 不为空返回用户名
	 */
	@Inner(value = false)
	@GetMapping("/details")
	public R getDetails(@ParameterObject SysUser query) {
		SysUser sysUser = userService.getOne(Wrappers.query(query), false);
		return R.ok(sysUser == null ? null : CommonConstants.SUCCESS);
	}

	/**
	 * 删除用户信息
	 * @param ids ID
	 * @return R
	 */
	@SysLog("删除用户信息")
	@DeleteMapping
	@PreAuthorize("@pms.hasPermission('sys_user_del')")
	@Operation(summary = "删除用户", description = "根据ID删除用户")
	public R userDel(@RequestBody Long[] ids) {
		return R.ok(userService.deleteUserByIds(ids));
	}

	/**
	 * 添加用户
	 * @param userDto 用户信息
	 * @return success/false
	 */
	@SysLog("添加用户")
	@PostMapping
	@PreAuthorize("@pms.hasPermission('sys_user_add')")
	public R user(@RequestBody UserDTO userDto) {
		return R.ok(userService.saveUser(userDto));
	}

	/**
	 * 更新用户信息
	 * @param userDto 用户信息
	 * @return R
	 */
	@SysLog("更新用户信息")
	@PutMapping
	@PreAuthorize("@pms.hasPermission('sys_user_edit')")
	public R updateUser(@Valid @RequestBody UserDTO userDto) {
		return R.ok(userService.updateUser(userDto));
	}

	/**
	 * 分页查询用户
	 * @param page 参数集
	 * @param userDTO 查询参数列表
	 * @return 用户集合
	 */
	@GetMapping("/page")
	public R getUserPage(@ParameterObject Page page, @ParameterObject UserDTO userDTO) {
		return R.ok(userService.getUsersWithRolePage(page, userDTO));
	}

	/**
	 * 修改个人信息
	 * @param userDto userDto
	 * @return success/false
	 */
	@SysLog("修改个人信息")
	@PutMapping("/edit")
	public R updateUserInfo(@Valid @RequestBody UserDTO userDto) {
		return userService.updateUserInfo(userDto);
	}

	/**
	 * 导出excel 表格
	 * @param userDTO 查询条件
	 * @return
	 */
	@ResponseExcel
	@GetMapping("/export")
	@PreAuthorize("@pms.hasPermission('sys_user_export')")
	public List export(UserDTO userDTO) {
		return userService.listUser(userDTO);
	}

	/**
	 * 导入用户
	 * @param excelVOList 用户列表
	 * @param bindingResult 错误信息列表
	 * @return R
	 */
	@PostMapping("/import")
	@PreAuthorize("@pms.hasPermission('sys_user_export')")
	public R importUser(@RequestExcel List<UserExcelVO> excelVOList, BindingResult bindingResult) {
		return userService.importUser(excelVOList, bindingResult);
	}

	/**
	 * 锁定指定用户
	 * @param username 用户名
	 * @return R
	 */
	@PutMapping("/lock/{username}")
	public R lockUser(@PathVariable String username) {
		return userService.lockUser(username);
	}

	@PutMapping("/password")
	public R password(@RequestBody UserDTO userDto) {
		String username = SecurityUtils.getUser().getUsername();
		userDto.setUsername(username);
		return userService.changePassword(userDto);
	}

	@PostMapping("/check")
	public R check(String password) {
		return userService.checkPassword(password);
	}


//	 @GetMapping("/parseUserExcel")
//	public R parseUserExcel () {
//		ExcelReaderBuilder builder = EasyExcel.read("C:\\Users\\ex_tangshk2\\Desktop\\深技服用户.xlsx");
//		List<Map<String, String>> userMap = builder.sheet("部门及用户").doReadSync();
//		userMap.forEach(map -> {
//			UserDTO userDTO = new UserDTO();
//			userDTO.setDisplayName(map.get(2));
//			userDTO.setUsername(map.get(1));
//			userDTO.setEmail(map.get(9));
//			String phone = map.get(10);
//			if (StringUtils.isNotBlank(phone) && phone.contains(";")) {
//				phone = phone.substring(0, phone.indexOf(";"));
//			}
//			userDTO.setPhone(phone);
//			userDTO.setPassword("HyW&&raxyunj&72G");
//			String deptName = map.get(5);
//			if (StringUtils.isBlank(deptName)) {
//				deptName = map.get(6);
//			}
//			if (deptName.contains(" \\ ")) {
//				deptName = deptName.substring(0, deptName.indexOf(" \\ "));
//			}
//			LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
//			wrapper.eq(SysDept::getName, deptName);
//			SysDept sysDept = deptService.getOne(wrapper);
//			if (Objects.isNull(sysDept)) {
//				sysDept = new SysDept();
//				sysDept.setName(deptName);
//				sysDept.setDelFlag("0");
//				deptService.save(sysDept);
//			}
//			userDTO.setDeptId(sysDept.getDeptId());
//			userService.registerUser(userDTO);
//		});
//		return R.ok();
//	}

	/**
	 * 根据关键字查找用户
	 *
	 * @param page
	 * @param username
	 * @return
	 */
	@GetMapping("/findUser/{current}/{size}")
	public R findUser(@ModelAttribute("page") Page<SysUser> page, @RequestParam(required = false) String username,
								@RequestParam(required = false) Long deptId) {
		return userService.findUser(username, deptId, page);
	}
}
