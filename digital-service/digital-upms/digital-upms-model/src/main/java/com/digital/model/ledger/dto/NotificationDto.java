package com.digital.model.ledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "系统消息")
public class NotificationDto implements Serializable {
    private static final long serialVersionUID = 4926047297660775247L;

    @Schema(description = "编号")
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "消息类型，1：系统消息 2：通知")
    private Integer type;

    @Schema(description = "事件")
    private String event;

    @Schema(description = "状态，已读，未读")
    private Integer status;

    @Schema(description = "接收者id列表，前端创建通知使用")
    private List<Long> receiverIds;

    @Schema(description = "接收者工号列表，后端台账发送通知使用")
    private List<String> receiverUsernames;
}
