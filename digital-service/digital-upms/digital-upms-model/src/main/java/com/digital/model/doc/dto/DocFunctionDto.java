package com.digital.model.doc.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "文档功能")
public class DocFunctionDto extends BaseDto{
    private static final long serialVersionUID = -4388145387559220639L;

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "前端路由地址")
    private String path;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "排序")
    @TableField(value = "`order`")
    private Integer order;
}
