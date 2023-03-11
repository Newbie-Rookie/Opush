package com.lin.opush.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 消息状态
 */
@Getter
@ToString
@AllArgsConstructor
public enum MessageStatus {
    /**
     * 10.新建
     */
    INIT(10, "初始化状态"),
    /**
     * 20.停用
     */
    STOP(20, "停用"),
    /**
     * 30.启用
     */
    RUN(30, "启用"),
    /**
     * 40.等待发送
     */
    PENDING(40, "等待发送"),
    /**
     * 50.发送中
     */
    SENDING(50, "发送中"),
    /**
     * 60.发送成功
     */
    SEND_SUCCESS(60, "发送成功"),
    /**
     * 70.发送失败
     */
    SEND_FAIL(70, "发送失败");

    private final Integer code;
    private final String description;
}
