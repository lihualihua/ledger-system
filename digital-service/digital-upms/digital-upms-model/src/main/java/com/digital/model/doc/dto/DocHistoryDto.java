package com.digital.model.doc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "智能搜索历史内容入参")
public class DocHistoryDto implements Serializable {
    private static final long serialVersionUID = -3379730223364547562L;

    @Schema(description = "搜索标识，''|0：全文，1：文件名，2：标签")
    private Integer sign;

    @Schema(description = "搜索内容")
    private String content;
}
