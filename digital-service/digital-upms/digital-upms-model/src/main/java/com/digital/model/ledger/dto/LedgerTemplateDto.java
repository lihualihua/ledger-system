package com.digital.model.ledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@Schema(description = "自定义模板入参")
public class LedgerTemplateDto implements Serializable {

    private static final long serialVersionUID = 5738598904290531851L;

    @NotBlank(message = "名称不能为空")
    @Schema(description = "名称")
    private String name;
}
