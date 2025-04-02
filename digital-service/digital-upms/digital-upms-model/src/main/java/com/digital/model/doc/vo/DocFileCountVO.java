package com.digital.model.doc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode
@Schema(description = "文件总数响应体")
public class DocFileCountVO implements Serializable {
    private static final long serialVersionUID = 76461938647898419L;

    @Schema(description = "公共文件总数")
    private Long commonFileCount;

    @Schema(description = "用户文件总数")
    private Long userFileCount;

    @Schema(description = "部门文件总数")
    private Long deptFileCount;
}
