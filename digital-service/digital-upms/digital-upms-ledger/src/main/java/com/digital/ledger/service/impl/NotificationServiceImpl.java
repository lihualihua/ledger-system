package com.digital.ledger.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.common.core.constant.enums.AsyncMessageTypeEnum;
import com.digital.common.core.exception.ApplicationException;
import com.digital.common.core.handler.IMessageSender;
import com.digital.common.core.model.AsyncMessage;
import com.digital.doc.helper.UserHelper;
import com.digital.ledger.enums.NotificationEnum;
import com.digital.ledger.mapper.NotificationMapper;
import com.digital.ledger.mapper.UserNotificationMapper;
import com.digital.ledger.service.INotificationService;
import com.digital.model.ledger.dto.NotificationDto;
import com.digital.model.ledger.entity.NotificationEntity;
import com.digital.model.ledger.entity.UserNotificationEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, NotificationEntity> implements INotificationService {

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private UserNotificationMapper userNotificationMapper;

    @Autowired
    private IMessageSender messageSender;

    @Override
    public void create(NotificationDto message) {
        NotificationEntity notification = buildMessageEntity(message);
        notification.setStatus(1);
        baseMapper.insert(notification);
        List<Long> receiverIds = message.getReceiverIds();
        if (CollectionUtils.isEmpty(receiverIds)) {
            return;
        }
        List<UserNotificationEntity> userNotificationList = new ArrayList<>();
        receiverIds.forEach(userId -> {
            UserNotificationEntity userNotification = new UserNotificationEntity();
            userNotification.setNotificationId(notification.getId());
            userNotification.setStatus(1);
            userNotification.setUserId(userId);
            userNotificationList.add(userNotification);
        });
        userNotificationMapper.insert(userNotificationList);
    }

    private NotificationEntity buildMessageEntity(NotificationDto message) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setTitle(message.getTitle());
        notificationEntity.setContent(message.getContent());
        notificationEntity.setEvent(message.getEvent());
        notificationEntity.setType(NotificationEnum.NOTIFICATION.getCode());
        notificationEntity.setUserId(userHelper.getUserId());
        return notificationEntity;
    }

    @Override
    public void updateReadStatus(Long id) {
        LambdaUpdateWrapper<UserNotificationEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserNotificationEntity::getNotificationId, id);
        wrapper.eq(UserNotificationEntity::getUserId, userHelper.getUserId());
        wrapper.set(UserNotificationEntity::getStatus, 0);
        userNotificationMapper.update(wrapper);
    }

    @Override
    public void updateAllReadStatus() {
        LambdaUpdateWrapper<UserNotificationEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserNotificationEntity::getUserId, userHelper.getUserId());
        wrapper.set(UserNotificationEntity::getStatus, 0);
        userNotificationMapper.update(wrapper);
    }

    @Override
    public IPage<NotificationEntity> findMyMessage(NotificationDto message, PageDTO page) {
        IPage<NotificationEntity> notificationPage = baseMapper.findMyNotification(page, message, userHelper.getUserId());
        if (Objects.isNull(notificationPage) || CollectionUtils.isEmpty(notificationPage.getRecords())) {
            return notificationPage;
        }

        notificationPage.getRecords().forEach(item -> {
            LocalDateTime createTime = item.getCreateTime();
            long between = DateUtil.between(Date.from(createTime.atZone( ZoneId.systemDefault()).toInstant()), new Date(), DateUnit.DAY);
            if (between == 0L) {
                item.setDateStr("今天");
            } else if (between == 1L) {
                item.setDateStr("昨天");
            } else if (between > 1L && between <= 7L ) {
                item.setDateStr("最近一周");
            } else if (between > 7L && between <= 14L ) {
                item.setDateStr("最近两周");
            } else if (between > 14L && between <= 31L ) {
                item.setDateStr("最近一个月");
            } else if (between > 31L && between <= 182L ) {
                item.setDateStr("最近半年");
            } else if (between > 182L && between <= 365L ) {
                item.setDateStr("最近一年");
            }  else {
                item.setDateStr("一年之前");
            }
        });
        return notificationPage;
    }


    @Override
    public void sendNotification(String title, String content, String event, List<String> receiverUsernames) {
        if (CollectionUtils.isEmpty(receiverUsernames)) {
            throw new ApplicationException("通知的用户信息为空!");
        }
        NotificationDto notification = new NotificationDto();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setReceiverUsernames(receiverUsernames);
        notification.setEvent(event);
        notification.setType(NotificationEnum.NOTIFICATION.getCode());
        AsyncMessage message = new AsyncMessage(AsyncMessageTypeEnum.NOTIFICATION.getType());
        message.setContent(notification);
        messageSender.sendMessage(message);
    }

    @Override
    public Map<String, Integer> findMyUnreadCount() {
        NotificationDto message = new NotificationDto();
        message.setStatus(1);
        Map<String, Integer> map = new HashMap<>();
        List<NotificationEntity> notificationList = baseMapper.findNotificationList(message, userHelper.getUserId());
        if (CollectionUtils.isEmpty(notificationList)) {
            map.put("unreadCount", 0);
        } else {
            map.put("unreadCount", notificationList.size());
        }
        return map;
    }

}
