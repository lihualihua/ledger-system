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

@Data
@Schema(description = "文档功能")
@EqualsAndHashCode(callSuper = true)
@TableName("doc_function_t")
public class DocFunctionEntity extends BaseEntity<DocFunctionEntity> {
    private static final long serialVersionUID = 7070052646700509305L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "编号")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "前端路由地址")
    private String path;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "排序")
    @TableField(value = "`order`")
    private Integer order;
}
