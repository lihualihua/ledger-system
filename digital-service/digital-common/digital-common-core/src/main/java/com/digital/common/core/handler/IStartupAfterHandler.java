package com.digital.common.core.handler;

import org.springframework.boot.context.event.ApplicationStartedEvent;

public interface IStartupAfterHandler {
    void execute(ApplicationStartedEvent event);
}
