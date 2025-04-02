package com.digital.model.nav.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class FolderInfoVO implements Serializable {
    private static final long serialVersionUID = 9033449344449185498L;

    @Schema(description = "文件夹ID")
    private Long fileId;

    @Schema(description = "文件夹名称")
    private String fileName;
}
