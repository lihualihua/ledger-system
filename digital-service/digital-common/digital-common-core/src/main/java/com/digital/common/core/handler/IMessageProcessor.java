package com.digital.common.core.handler;

import com.digital.common.core.exception.ApplicationException;
import com.digital.common.core.model.AsyncMessage;

public interface IMessageProcessor {

    String getType();

    void process(AsyncMessage message) throws ApplicationException;
}
