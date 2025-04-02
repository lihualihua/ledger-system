package com.digital.model.doc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "批量操作")
public class BatchDto<T extends BaseDto> implements Serializable {
    private static final long serialVersionUID = 3064837735850231893L;

    @Valid
    @Schema(description = "批量创建")
    private List<T> batchCreateList;

    @Valid
    @Schema(description = "批量修改")
    private List<T> batchUpdateList;

    @Valid
    @Schema(description = "批量删除")
    private List<T> batchDeleteList;
}
