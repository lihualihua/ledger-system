package com.digital.model.ledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "台账")
@EqualsAndHashCode(callSuper = true)
@TableName("led_ledger_t")
public class LedgerEntity extends BaseEntity<LedgerEntity> {

    private static final long serialVersionUID = 822965250861345537L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private Long id;

    @Schema(description = "文件id")
    private Long fileId;

    @Schema(description = "模板id")
    private String tempId;

    @Schema(description = "模板库id")
    private Long templateId;

    @Schema(description = "类型(1:按行,2:单元格)")
    private Integer type;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "sheet名称")
    private String sheetName;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "状态(1:发布,2:归档,3:作废)")
    private Integer status;

    @Schema(description = "创建人id")
    private Long createById;

    @Schema(description = "数据状态(0:删除，1:生效)")
    private Integer delFlag;

}
