package com.digital.model.doc.response;

import com.digital.model.doc.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "智能搜索查询标签响应体")
public class DocTagResponse extends BaseDto {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "标签名称")
    private String name;
}
