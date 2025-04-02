package com.digital.model.doc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "文件夹入参")
public class DocFileDto implements Serializable {
    private static final long serialVersionUID = 8244302518808802476L;

    @Schema(description = "文件夹编号")
    private Long fileId;

    @Schema(description = "部门ID")
    private Long deptId;

    /**
     * 文件夹名称
     */
    @NotBlank(message = "名称必填")
    @Schema(description = "文件名称")
    private String fileName;

    /**
     * 文件夹父级编号
     */
    @NotNull(message = "父级编号必填")
    @Schema(description = "父级编号")
    private Long parentId;

    @NotNull(message = "归属必填")
    @Schema(description = "归属")
    private Integer ownership;

    @Schema(description = "删除标记")
    private Integer delFlag;

    @Schema(description = "文件夹标记")
    private Integer folderFlag;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "类型")
    private String fileType;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "子文件")
    private List<DocFileDto> child;

    @Schema(description = "编辑标志，1：超级管理员，2：部门管理员及以上，3：个人，4：所有人, 5：不可编辑")
    private Integer editFlag;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "文件路径")
    private String filePath;
}
