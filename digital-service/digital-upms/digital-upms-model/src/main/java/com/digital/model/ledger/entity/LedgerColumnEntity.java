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
@TableName("led_column_t")
public class LedgerColumnEntity extends BaseEntity<LedgerColumnEntity> {

    private static final long serialVersionUID = -5808749297930093498L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private Long id;

    @Schema(description = "模板id")
    private String tempId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "创建人id")
    private Long createById;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否固定列,(0:否,1：是)")
    private Integer isFixed;

}
