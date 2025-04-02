package com.digital.model.doc.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 搜索历史内容
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
@Data
@Schema(description = "搜索历史内容")
@EqualsAndHashCode(callSuper = true)
@TableName("doc_history_search_t")
public class DocHistoryEntity extends BaseEntity<DocHistoryEntity> {
	private static final long serialVersionUID = -6248311447377802183L;

	@Schema(description = "id")
	private Long id;

	@Schema(description = "用户id")
	private Long userId;

	@Schema(description = "搜索内容")
	private String content;

	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建人")
	private String createBy;

	@JsonIgnore
	@Schema(description = "创建时间")
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@JsonIgnore
	@Schema(description = "更新人")
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateBy;

	@JsonIgnore
	@Schema(description = "更新时间")
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
