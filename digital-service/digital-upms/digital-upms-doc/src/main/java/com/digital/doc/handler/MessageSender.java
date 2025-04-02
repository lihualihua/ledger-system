package com.digital.doc.handler;

import com.digital.common.core.handler.IThreadPoolHandler;
import com.digital.common.core.handler.IMessageSender;
import com.digital.common.core.model.AsyncMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageSender implements IMessageSender {

    @Autowired
    private IThreadPoolHandler threadPoolHandler;

    @Override
    public void sendMessage(AsyncMessage message) {
        threadPoolHandler.execute(message);
    }
}
