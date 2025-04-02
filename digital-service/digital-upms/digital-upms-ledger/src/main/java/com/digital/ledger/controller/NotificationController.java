package com.digital.ledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.digital.common.core.util.R;
import com.digital.ledger.service.INotificationService;
import com.digital.model.ledger.dto.NotificationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private INotificationService notificationService;

    /**
     * 创建系统通知
     *
     * @param notification
     * @return
     */
    @PostMapping("/create")
    public R create(@Valid @RequestBody NotificationDto notification) {
        notificationService.create(notification);
        return R.ok();
    }

    /**
     * 更新通知为已读状态
     *
     * @param id
     * @return
     */
    @PostMapping("/updateReadStatus/{id}")
    public R updateReadStatus(@PathVariable Long id) {
        notificationService.updateReadStatus(id);
        return R.ok();
    }

    /**
     * 全部已读
     *
     * @return
     */
    @PostMapping("/updateAllReadStatus")
    public R updateAllReadStatus() {
        notificationService.updateAllReadStatus();
        return R.ok();
    }

    /**
     * 查找未读通知总数
     *
     * @return
     */
    @GetMapping("/findMyUnreadCount")
    public R findMyUnreadCount() {
        return R.ok(notificationService.findMyUnreadCount());
    }

    /**
     * 查找我的消息通知
     *
     * @param notification
     * @param page
     * @return
     */
    @PostMapping("/findMyMessage/{current}/{size}")
    public R findMyMessage(@RequestBody(required = false) NotificationDto notification, @ModelAttribute PageDTO page) {
        return R.ok(notificationService.findMyMessage(notification, page));
    }
}
