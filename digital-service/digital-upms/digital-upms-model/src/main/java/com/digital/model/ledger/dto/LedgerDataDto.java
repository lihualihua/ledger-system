package com.digital.model.ledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Schema(description = "系统消息")
public class LedgerDataDto implements Serializable {
    private static final long serialVersionUID = 1823926231798664531L;

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "内容")
    private String value;

    @Schema(description = "行编号")
    @NotNull(message = "行编号不能为空")
    private Long rowId;

    @Schema(description = "字段编号")
    @NotNull(message = "字段编号不能为空")
    private Long columnId;
}
