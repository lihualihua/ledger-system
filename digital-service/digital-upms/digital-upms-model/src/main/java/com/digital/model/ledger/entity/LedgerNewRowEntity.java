package com.digital.model.ledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "缩表后的台账坐标")
@EqualsAndHashCode(callSuper = true)
@TableName("led_row_new_t")
public class LedgerNewRowEntity extends BaseEntity<LedgerNewRowEntity> {
    private static final long serialVersionUID = 2106941614617863524L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private Long id;

    @Schema(description = "模板id")
    private String tempId;

    @Schema(description = "列坐标")
    private Long columnId;

    @Schema(description = "旧行坐标")
    private Long oldRowId;

    @Schema(description = "新行坐标")
    private Long newRowId;

    @Schema(description = "编辑人")
    private String assigner;
}
