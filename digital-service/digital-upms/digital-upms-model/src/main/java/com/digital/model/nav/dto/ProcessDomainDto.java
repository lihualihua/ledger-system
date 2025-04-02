package com.digital.model.nav.dto;

import com.digital.model.doc.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流程域")
public class ProcessDomainDto extends BaseDto {
    private static final long serialVersionUID = 9091232858421992386L;

    @Schema(description = "编号")
    private Long id;

    @NotBlank(message = "流程域名称不能为空")
    @Schema(description = "名称")
    private String name;

    @Schema(description = "描述")
    private String desc;

    @Schema(description = "顺序")
    private Integer order;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "存储目录ID")
    @NotNull(message = "存储目录不能为空")
    private Long folderId;
}
