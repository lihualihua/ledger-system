package com.digital.model.doc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode
@Schema(description = "文件大小总和响应体")
public class DocFileSizeSumVO implements Serializable {
    private static final long serialVersionUID = -5827662149632482046L;

    @Schema(description = "文件大小总和")
    private Long fileSizeSum;

    @Schema(description = "文件大小总和，带单位")
    private String fileSizeSumStr;
}
