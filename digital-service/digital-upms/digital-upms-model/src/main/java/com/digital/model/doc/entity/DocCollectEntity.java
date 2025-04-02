package com.digital.model.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
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
@Schema(description = "文件")
@EqualsAndHashCode(callSuper = true)
@TableName("doc_collect_t")
public class DocCollectEntity extends BaseEntity<DocCollectEntity> {
	private static final long serialVersionUID = 8153010720422100973L;

	@TableId(type = IdType.AUTO)
	@Schema(description = "收藏编号")
	private Long id;

	@Schema(description = "用户编号")
	private Long userId;

	@Schema(description = "文件编号")
	private Long fileId;

	@Schema(description = "状态")
	private Integer status;
}
