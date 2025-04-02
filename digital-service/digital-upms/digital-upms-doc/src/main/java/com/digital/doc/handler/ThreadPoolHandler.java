package com.digital.doc.handler;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.digital.common.core.handler.IMessageProcessor;
import com.digital.common.core.handler.IStartupAfterHandler;
import com.digital.common.core.handler.IThreadPoolHandler;
import com.digital.common.core.model.AsyncMessage;
import com.digital.doc.config.ThreadPoolConfig;
import com.digital.doc.helper.UserHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ThreadPoolHandler implements IThreadPoolHandler, IStartupAfterHandler, DisposableBean {
    private static final Logger LOGGER = LogManager.getLogger(ThreadPoolHandler.class);

    @Autowired
    private ThreadPoolConfig config;

    @Autowired
    private AsyncProcessorCollectionHandler handler;

    @Autowired
    private UserHelper userHelper;

    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void execute(Runnable command) {
        threadPoolExecutor.execute(command);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return threadPoolExecutor.submit(task);
    }

    @Override
    public void execute(AsyncMessage message) {
        Authentication authentication = userHelper.getAuthentication();
        this.execute(() -> {
            String type = message.getType();
            try {
                IMessageProcessor processor = handler.getProcessor(type);
                if (Objects.isNull(processor)) {
                    LOGGER.info("异步消息的类型不存在！ type is : {}", type);
                    return;
                }
                userHelper.setAuthentication(authentication);
                processor.process(message);
            } catch (Exception ex) {
                LOGGER.error("异步消息处理异常，type is : {}", type);
                LOGGER.error(ex);
            }
        });
    }

    @Override
    public void destroy() {
        if (Objects.nonNull(threadPoolExecutor)) {
            threadPoolExecutor.shutdown();
        }
    }

    @Override
    public void execute(ApplicationStartedEvent event) {
        initialize();
    }

    private void initialize() {
        threadPoolExecutor = new ThreadPoolExecutor(config.getCorePoolSize(), config.getMaximumPoolSize(), config.getKeepAliveTime(),
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(config.getCapacity()));
        ThreadFactory factory = new ThreadFactoryBuilder().setNamePrefix("digital-thread-pool-").build();
        threadPoolExecutor.setThreadFactory(factory);
        threadPoolExecutor.setRejectedExecutionHandler(
                (r, executor) -> LOGGER.error("The thread has executed the deny policy and the task will be lost."));
    }
}
