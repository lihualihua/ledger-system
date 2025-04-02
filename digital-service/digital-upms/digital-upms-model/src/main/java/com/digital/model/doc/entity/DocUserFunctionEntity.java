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
@Schema(description = "用户关联的功能")
@EqualsAndHashCode(callSuper = true)
@TableName("doc_user_function_t")
public class DocUserFunctionEntity extends BaseEntity<DocUserFunctionEntity> {
    private static final long serialVersionUID = 7070052646700509305L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "编号")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "功能编号")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long functionId;

    @Schema(description = "用户编号")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
}
