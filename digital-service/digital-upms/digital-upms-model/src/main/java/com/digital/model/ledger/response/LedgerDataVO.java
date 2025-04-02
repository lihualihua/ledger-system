package com.digital.model.ledger.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "模板数据响应体")
public class LedgerDataVO implements Serializable {
    private static final long serialVersionUID = 2305181974478266838L;

    @Schema(description = "表头")
    private List<LedgerColumnVO> columns;

    @Schema(description = "数据")
    private List<Map<String, LedgerCellVO>> data;
}
