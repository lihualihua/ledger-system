package com.digital.model.doc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "标签")
public class DocTagDto extends BaseDto {
    private static final long serialVersionUID = 6618496207353004086L;

    @Schema(description = "标签编号")
    private Long id;

    @NotBlank(message = "标签名称不能为空")
    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "智能搜索标签名称List")
    private List<String> tag;

    @Schema(description = "标签描述")
    private String desc;

    @Schema(description = "标签状态")
    private Integer status;

    @Schema(description = "文件夹编号")
    private Long parentId;

    @Schema(description = "文件名称")
    private String fileName;
}
