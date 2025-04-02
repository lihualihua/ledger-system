package com.digital.model.ledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "台账列字段")
@EqualsAndHashCode(callSuper = true)
@TableName("led_assigner_t")
public class LedgerAssignerEntity extends BaseEntity<LedgerAssignerEntity> {
    private static final long serialVersionUID = 1731612787448416515L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private Long id;

    @Schema(description = "台账ID")
    private String tempId;

    @Schema(description = "分配人")
    private String assigner;

    @Schema(description = "总数")
    private int total;

    @Schema(description = "当前数量")
    private int current;

    @Schema(description = "状态，1：未处理，0：已处理")
    private int status;
}
