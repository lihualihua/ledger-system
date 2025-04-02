package com.digital.model.doc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 文件搜索
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
@Data
@Schema(description = "文件搜索")
@EqualsAndHashCode(callSuper = true)
@TableName("doc_file_t")
public class DocSearchEntity extends DocFileEntity {
	private static final long serialVersionUID = -7892354226654643524L;

	@JsonIgnore
	@Schema(description = "父级文件夹id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long parentId;

	@JsonIgnore
	@Schema(description = "修改人")
	private String updateBy;

	@JsonIgnore
	@Schema(description = "文件大小,带单位")
	private String fileSizeStr;

	@JsonIgnore
	@TableField(exist = false)
	private Integer collectFlag;

	@JsonIgnore
	@TableField(exist = false)
	@Schema(description = "标签")
	private List<String> tag;

	@JsonIgnore
	@TableField(select = false)
	@Schema(description = "搜索类型（base:基础,advanced:高级）")
	private String searchType;

	@JsonIgnore
	@TableField(select = false)
	@Schema(description = "标签and及or")
	private String tagAndOr;

	@JsonIgnore
	@TableField(select = false)
	@Schema(description = "创建人and及or")
	private String createByAndOr;

	@JsonIgnore
	@TableField(select = false)
	@Schema(description = "起始时间")
	private String startTime;

	@JsonIgnore
	@TableField(select = false)
	@Schema(description = "结束时间")
	private String endTime;

	@JsonIgnore
	@TableField(select = false)
	@Schema(description = "更新时间范围")
	private Integer updateTimeSlot;

	@JsonIgnore
	@Schema(description = "编辑标志，1：超级管理员，2：部门管理员及以上，3：个人，4：所有人, 5：不可编辑")
	private Integer editFlag;

	@TableField(select = false)
	@Schema(description = "高亮显示")
	private Map<String, List<String>> highLight;
}
