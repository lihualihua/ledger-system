package com.digital.model.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "用户文件")
@EqualsAndHashCode(callSuper = true)
@TableName("doc_user_file_t")
public class UserFileEntity extends BaseEntity<UserFileEntity> {
    private static final long serialVersionUID = -6758931998137267614L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "编号")
    private Long id;

    @Schema(description = "用户编号")
    private Long userId;

    @Schema(description = "文件编号")
    private Long fileId;
}
