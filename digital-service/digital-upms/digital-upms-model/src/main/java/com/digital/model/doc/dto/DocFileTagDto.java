package com.digital.model.doc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文档标签")
public class DocFileTagDto extends BaseDto {
    private static final long serialVersionUID = -8428624432748933769L;

    @Schema(description = "标签编号列表")
    private List<Long> tagIds;

    @NotNull(message = "文件编号列表不能为空")
    @Schema(description = "文件编号列表")
    private List<Long> fileIds;
}
