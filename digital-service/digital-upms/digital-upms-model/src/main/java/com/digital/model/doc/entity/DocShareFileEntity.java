package com.digital.model.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "文档分享")
@EqualsAndHashCode(callSuper = true)
@TableName("doc_share_file_t")
public class DocShareFileEntity extends BaseEntity<DocShareFileEntity> {
    private static final long serialVersionUID = 4797999845052108152L;

    /**
     * 编号
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "编号")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "分享编号")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shareId;

    @Schema(description = "文件编号")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fileId;
}
