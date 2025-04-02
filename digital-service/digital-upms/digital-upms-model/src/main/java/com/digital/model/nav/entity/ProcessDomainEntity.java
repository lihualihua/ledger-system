package com.digital.model.nav.entity;

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
@Schema(description = "流程域实体")
@EqualsAndHashCode(callSuper = true)
@TableName("nav_process_domain_t")
public class ProcessDomainEntity extends BaseEntity<ProcessDomainEntity> {
    private static final long serialVersionUID = 5719926065235509783L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "编号")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "描述")
    @TableField(value = "`desc`")
    private String desc;

    @Schema(description = "顺序")
    @TableField(value = "`order`")
    private Integer order;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "附件存储目录ID")
    private Long folderId;

    @TableField(exist = false)
    @Schema(description = "附件存储位置")
    private String location;
}
