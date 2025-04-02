package com.digital.common.core.handler;

import com.digital.common.core.model.AsyncMessage;

public interface IMessageSender {

    /**
     * 发送异步消息
     *
     * @param message
     */
    void sendMessage(AsyncMessage message);
}
