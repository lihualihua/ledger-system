package com.digital.model.doc.vo;

import com.digital.model.doc.entity.DocTagEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode
@Schema(description = "文件明细信息响应体")
public class DocFileDetailVO implements Serializable {
    private static final long serialVersionUID = -2847460826509462717L;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "文件夹标记,1：文件夹")
    private Integer folderFlag;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "文件大小总和")
    private Long fileSizeSum;

    @Schema(description = "文件大小总和，带单位")
    private String fileSizeSumStr;

    @Schema(description = "文件总数")
    private int fileCount;

    @Schema(description = "文件总数")
    private int folderCount;

    @Schema(description = "文件位置")
    private String location;

    @Schema(description = "文件添加的标签列表")
    @JsonIgnoreProperties({"userId", "status", "desc", "publicFlag", "createBy", "createTime", "updateBy", "updateTime"})
    private List<DocTagEntity> tags;

    @Schema(description = "下载地址")
    private String downloadUrl;
}
