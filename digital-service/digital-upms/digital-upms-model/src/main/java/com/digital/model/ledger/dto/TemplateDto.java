package com.digital.model.ledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@Schema(description = "模板")
public class TemplateDto implements Serializable {
    private static final long serialVersionUID = -3154144859978431923L;

    @NotBlank(message = "模板编号不能为空")
    @Schema(description = "模板ID")
    private String tempId;

    @Schema(description = "是否过滤自带列")
    private Integer isFixed;
}
