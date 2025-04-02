package com.digital.doc.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ThreadPoolConfig {

    /**
     * 核心线程数
     */
    @Value("${digital.corePoolSize:10}")
    private int corePoolSize;

    /**
     * 最大线程数
     */
    @Value("${digital.maximumPoolSize:20}")
    private int maximumPoolSize;

    /**
     * 空闲线程存活时间，单位：秒
     */
    @Value("${digital.keepAliveTime:60}")
    private long keepAliveTime;

    /**
     * 队列空间
     */
    @Value("${digital.capacity:500}")
    private int capacity;
}
