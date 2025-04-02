package com.digital.model.ledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "台账自定义模板")
@EqualsAndHashCode(callSuper = true)
@TableName("led_template_t")
public class LedgerTemplateEntity extends BaseEntity<LedgerTemplateEntity> {

    private static final long serialVersionUID = -1585325632733458463L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "系统自带(0:否，1:是)")
    private Integer systemFlag;
}
