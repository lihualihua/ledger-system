package com.digital.ledger.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.digital.model.ledger.dto.NotificationDto;
import com.digital.model.ledger.entity.NotificationEntity;

import java.util.List;
import java.util.Map;

public interface INotificationService extends IService<NotificationEntity> {

    /**
     * 创建消息
     *
     * @param message
     */
    void create(NotificationDto message);

    /**
     * 修改已读状态
     *
     * @param id
     */
    void updateReadStatus(Long id);

    /**
     * 全部已读
     */
    void updateAllReadStatus();

    /**
     * 查找我的消息
     *
     * @param message
     * @return
     */
    IPage<NotificationEntity> findMyMessage(NotificationDto message, PageDTO page);

    /**
     * 发送通知
     *
     * @param title 标题
     * @param content 内容
     * @param receiverUsernames 接收者id列表
     */
    void sendNotification(String title, String content, String event, List<String> receiverUsernames);

    Map<String, Integer> findMyUnreadCount();
}
