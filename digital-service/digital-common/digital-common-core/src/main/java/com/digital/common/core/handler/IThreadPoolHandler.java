package com.digital.common.core.handler;

import com.digital.common.core.model.AsyncMessage;

import java.util.concurrent.Future;

public interface IThreadPoolHandler {
    void execute(Runnable command);

    Future<?> submit(Runnable task);

    void execute(AsyncMessage message);
}
