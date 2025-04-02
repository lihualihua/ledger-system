package com.digital.model.ledger.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "台账单元格响应体")
public class LedgerCellVO implements Serializable {
    private static final long serialVersionUID = -4185865383870335715L;

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "行编号")
    private Long rowId;

    @Schema(description = "字段编号")
    private Long columnId;

    @Schema(description = "内容")
    private String value;

    @Schema(description = "是否可编辑")
    private boolean isEdit;
}
