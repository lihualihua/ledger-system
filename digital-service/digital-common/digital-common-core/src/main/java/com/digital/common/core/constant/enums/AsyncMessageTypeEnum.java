package com.digital.common.core.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AsyncMessageTypeEnum {

    /**
     * 通知
     */
    NOTIFICATION("notification", "消息通知");

    /**
     * 类型
     */
    private final String type;

    /**
     * 描述
     */
    private final String desc;
}
