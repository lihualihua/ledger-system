package com.digital.model.ledger.response;

import com.digital.model.doc.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "自定义模板响应体")
public class LedgerTemplateListVO extends BaseDto {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "系统自带(0:否，1:是)")
    private Integer systemFlag;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
