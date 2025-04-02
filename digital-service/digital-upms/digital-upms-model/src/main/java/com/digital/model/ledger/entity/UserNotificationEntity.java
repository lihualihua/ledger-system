package com.digital.model.ledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "用户系统消息")
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_notifications")
public class UserNotificationEntity extends BaseEntity<UserNotificationEntity> {
    private static final long serialVersionUID = 7290459497048305447L;

    @Schema(description = "编号")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "接收者编号")
    private Long userId;

    @Schema(description = "接收者编号")
    private Long notificationId;

    @Schema(description = "状态，1：未读，0：已读")
    private Integer status;
}
