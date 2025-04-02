package com.digital.doc.handler;

import com.digital.common.core.handler.IMessageProcessor;
import com.digital.common.core.handler.IStartupAfterHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class AsyncProcessorCollectionHandler implements IStartupAfterHandler {
    private static final Logger LOGGER = LogManager.getLogger(AsyncProcessorCollectionHandler.class);

    private final Map<String, IMessageProcessor> processorMap = new HashMap<>();

    @Override
    public void execute(ApplicationStartedEvent event) {
        Map<String, IMessageProcessor> processors = event.getApplicationContext().getBeansOfType(IMessageProcessor.class);
        if (processors.isEmpty()) {
            return;
        }
        processors.values().forEach(bean -> {
            processorMap.put(bean.getType(), bean);
        });
        LOGGER.info("应用启动后收集异步消息处理器成功.");
    }

    /**
     * 获取异步消息类型
     *
     * @return
     */
    public Set<String> getAsyncMessageTypes() {
        return processorMap.keySet();
    }

    /**
     * 根据类型获取异步消息处理器
     *
     * @param type
     * @return
     */
    public IMessageProcessor getProcessor(String type) {
        return processorMap.get(type);
    }
}
