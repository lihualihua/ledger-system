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

package com.digital.model.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 文件管理
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
@Data
@Schema(description = "文件")
@EqualsAndHashCode(callSuper = true)
@TableName("doc_file_t")
public class DocFileEntity extends BaseEntity<DocFileEntity> {
private static final long serialVersionUID = 2914876531085934272L;

@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "文件编号")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long fileId;

	@Schema(description = "文件名")
	private String fileName;

	@Schema(description = "文件类型")
	private String fileType;

	@Schema(description = "文件大小")
	private Long fileSize;

	@Schema(description = "文件大小,带单位")
	private String fileSizeStr;

	@JsonIgnore
	@Schema(description = "文件路径")
	private String filePath;

	@Schema(description = "删除标记,0:已删除,1:正常")
	private Integer delFlag;

	@Schema(description = "文件夹标记,1：文件夹")
	private Integer folderFlag;

	@Schema(description = "文件归属,1:公共，2：用户，3：部门")
	private Integer ownership;

	@Schema(description = "父级文件夹id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long parentId;

	@Schema(description = "下载量")
	private Integer downloadCount;

	@Schema(description = "收藏量")
	private Integer collectCount;

	@Schema(description = "预览量")
	private Integer previewCount;

	@Schema(description = "图标")
	private String icon;

	// TODO: 智能搜索ES返回字段 （es需使用该字段，但会导致智能搜索不返回createBy，待确认不加@JsonIgnore对其他功能是否有影响）
/* 	@JsonIgnore
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建人")
	 private String createBy;*/

	@JsonIgnore
	@Schema(description = "上传人id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long createById;

	@JsonIgnore
	@TableField(exist = false)
	@Schema(description = "分类")
	private String category;

	@JsonIgnore
	@TableField(exist = false)
	@Schema(description = "文件类型集合")
	private List<String> fileTypeList;

	@TableField(exist = false)
	@Schema(description = "收藏标记，1：已收藏，0：未收藏")
	private Integer collectFlag = 0;

	@Schema(description = "编辑标志，1：超级管理员，2：部门管理员及以上，3：个人，4：所有人, 5：不可编辑")
	private Integer editFlag;
}
