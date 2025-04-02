package com.digital.doc.handler;

import com.digital.common.core.handler.IStartupAfterHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationStartupAfterHandler implements ApplicationListener<ApplicationStartedEvent> {
    private static final Logger LOGGER = LogManager.getLogger(ApplicationStartupAfterHandler.class);

    @Autowired(required = false)
    private List<IStartupAfterHandler> startupAfterHandlers;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        LOGGER.info("After the application is started, execute the post-startup event.");
        if (CollectionUtils.isEmpty(startupAfterHandlers)) {
            return;
        }
        startupAfterHandlers.forEach(item -> {
            try {
                item.execute(event);
            } catch (Exception ex) {
                LOGGER.error("Event execution failed after application startup.");
                LOGGER.error(ex);
            }
        });
    }
}
