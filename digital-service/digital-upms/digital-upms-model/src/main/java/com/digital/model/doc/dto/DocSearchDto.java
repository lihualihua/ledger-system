package com.digital.model.doc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "智能搜索入参")
public class DocSearchDto implements Serializable {
    private static final long serialVersionUID = -6144239152707645862L;

    @Schema(description = "搜索类型（基础/高级）")
    private String searchType;

    @Schema(description = "文件夹编号")
    private Long fileId;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "父级编号")
    private Long parentId;

    @Schema(description = "部门ID")
    private Long deptId;

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

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建时间")
    private LocalDateTime updateTime;

    @Schema(description = "标签")
    private List<String> tag;

    @Schema(description = "标签and及or")
    private String tagAndOr;

    @Schema(description = "创建人and及or")
    private String createByAndOr;

    @Schema(description = "起始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "更新时间范围")
    private Integer updateTimeSlot;

}
