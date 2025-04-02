package com.digital.model.doc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "文档分享")
public class DocShareDto implements Serializable {
    private static final long serialVersionUID = 2081368680890287831L;

    @Schema(description = "分享编号")
    private Long id;

    @Schema(description = "分享名称")
    @NotBlank(message = "分享名称不能为空")
    private String name;

    @NotNull(message = "文件编号不能为空")
    @Schema(description = "文件编号")
    private List<Long> fileIds;

    @NotNull(message = "有效期标识不能为空")
    @Range(min = 1, max = 5, message = "有效期标识不正确")
    @Schema(description = "有效期标识")
    private Integer validFlag;

    @Schema(description = "有效期")
    private String validDate;

    @Schema(description = "分享用户")
    private List<Long> shareUserIds;

    @Schema(description = "分享部门")
    private List<Long> shareDeptIds;

    @Schema(description = "描述")
    private String desc;

    @Schema(description = "访问模式")
    private Integer accessMode;

    @Schema(description = "md5")
    private String md5Value;

    @Schema(description = "文件夹编号")
    private Long parentId;
}
