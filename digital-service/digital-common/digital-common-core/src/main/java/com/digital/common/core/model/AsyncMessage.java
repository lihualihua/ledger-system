package com.digital.common.core.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class AsyncMessage implements Serializable {

    private static final long serialVersionUID = -7303401480131348611L;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息内容
     */
    private Serializable content;

    /**
     * 消息上下文
     */
    private Map<String, Object> context;

    public AsyncMessage(String type) {
        this.type = type;
    }
}
