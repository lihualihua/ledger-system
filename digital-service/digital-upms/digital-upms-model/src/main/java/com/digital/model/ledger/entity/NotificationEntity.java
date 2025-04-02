package com.digital.model.ledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "系统消息")
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notifications")
public class NotificationEntity extends BaseEntity<NotificationEntity> {
    private static final long serialVersionUID = 7290459497048305447L;

    @Schema(description = "编号")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "发送者编号")
    private Long userId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "消息类型，1：系统消息，取当前表状态，2：通知，取user_notifications表状态")
    private Integer type;

    @Schema(description = "事件")
    private String event;

    @Schema(description = "状态，1：未读，0：已读")
    private Integer status;

    @TableField(exist = false)
    @Schema(description = "日期字符串，一天前，一周前，一个月前")
    private String dateStr;
}
