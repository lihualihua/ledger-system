package com.digital.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.digital.model.ledger.dto.NotificationDto;
import com.digital.model.ledger.entity.NotificationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档收藏
 */
@Mapper
public interface NotificationMapper extends BaseMapper<NotificationEntity> {
    IPage<NotificationEntity> findMyNotification(PageDTO page, @Param("notification") NotificationDto notification, @Param("userId") Long userId);

    List<NotificationEntity> findNotificationList(@Param("notification") NotificationDto notification, @Param("userId") Long userId);
}
