package com.digital.model.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件管理
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
@Data
@Schema(description = "标签")
@EqualsAndHashCode(callSuper = true)
@TableName("doc_tag_t")
public class DocTagEntity extends BaseEntity<DocTagEntity> {
	private static final long serialVersionUID = 8466239513043359514L;
	/**
	 * 编号
	 */
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "标签编号")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	@Schema(description = "用户编号")
	private Long userId;

	@Schema(description = "名称")
	private String name;

	@Schema(description = "状态")
	private Integer status;

	@Schema(description = "描述")
	@TableField(value = "`desc`")
	private String desc;

	@Schema(description = "公开标记，0：不公开，1：内部公开(包含当前部门公开)，2：部门公开")
	private Integer publicFlag;
}
