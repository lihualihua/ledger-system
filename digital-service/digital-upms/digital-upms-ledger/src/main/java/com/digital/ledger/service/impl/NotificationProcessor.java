package com.digital.ledger.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.digital.admin.service.SysUserService;
import com.digital.common.core.constant.enums.AsyncMessageTypeEnum;
import com.digital.common.core.handler.IMessageProcessor;
import com.digital.common.core.model.AsyncMessage;
import com.digital.ledger.service.INotificationService;
import com.digital.model.admin.entity.SysUser;
import com.digital.model.ledger.dto.NotificationDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class NotificationProcessor implements IMessageProcessor {
    private static final Logger LOGGER = LogManager.getLogger(NotificationProcessor.class);

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private SysUserService userService;

    @Override
    public String getType() {
        return AsyncMessageTypeEnum.NOTIFICATION.getType();
    }

    @Override
    public void process(AsyncMessage message) {
        NotificationDto dto = (NotificationDto) message.getContent();
        List<Long> userIds = new ArrayList<>();
        dto.getReceiverUsernames().forEach(item -> {
            SysUser user = userService.getOne(Wrappers.<SysUser>query().lambda().eq(SysUser::getUsername, item));
            if (Objects.isNull(user)) {
                return;
            }
            userIds.add(user.getUserId());
        });
        dto.setReceiverIds(userIds);
        notificationService.create(dto);
        LOGGER.info("异步创建通知成功.");
    }
}
