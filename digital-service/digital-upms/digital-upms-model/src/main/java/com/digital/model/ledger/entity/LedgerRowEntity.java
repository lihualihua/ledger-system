package com.digital.model.ledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "台账坐标")
@EqualsAndHashCode(callSuper = true)
@TableName("led_row_t")
public class LedgerRowEntity extends BaseEntity<LedgerRowEntity> {

    private static final long serialVersionUID = 552513120765629848L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private Long id;

    @Schema(description = "模板id")
    private String tempId;

    @Schema(description = "类型(1:按行,2:单元格)")
    private Integer type;

    @Schema(description = "列坐标")
    private Long columnId;

    @Schema(description = "行坐标")
    private Long rowId;

    @Schema(description = "值")
    private String content;

    @Schema(description = "是否可见(0:不可见,1:可见)")
    private Integer isView;

    @Schema(description = "是否可编辑(0:不可编辑,1:可编辑)")
    private Integer isEdit;

    @Schema(description = "编辑人")
    private String assigner;

    @Schema(description = "创建人id")
    private Long createById;

}
