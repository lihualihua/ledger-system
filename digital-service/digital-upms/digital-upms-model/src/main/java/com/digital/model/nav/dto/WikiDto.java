package com.digital.model.nav.dto;

import com.digital.model.doc.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "wiki")
public class WikiDto extends BaseDto {
    private static final long serialVersionUID = -4444978173854401347L;

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "排序")
    private Integer order;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "wiki原始内容，未格式化")
    private String originalContent;

    @Schema(description = "描述")
    private String desc;

    @Size(min = 2, max = 10, message = "版本号长度为2-10")
    @Schema(description = "版本")
    private String version;

    @Schema(description = "领域编号")
    private Long domainId;
}
