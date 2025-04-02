package com.digital.model.nav.dto;

import com.digital.model.doc.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "WikiAtt")
public class WikiAttDto extends BaseDto {
    private static final long serialVersionUID = 7577096429589329803L;

    @Schema(description = "文件编号列表")
    @NotNull(message = "wiki模板文件编号列表不能为空")
    private List<Long> fileIds;

    @Schema(description = "wiki编号")
    @NotNull(message = "wiki编号不能为空")
    private Long wikiId;
}
