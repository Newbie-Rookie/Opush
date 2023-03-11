package com.lin.opush.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 消息业务类型常量
 */
@Getter
@ToString
@AllArgsConstructor
public enum BusinessCode {
    /**
     * 普通发送业务
     */
    COMMON_SEND("send", "普通发送"),
    /**
     * 撤回业务
     */
    RECALL("recall", "撤回消息");
    /**
     * 关联不同的执行链 → ProcessChain
     *      关联关系存储在Map中（com.lin.opush.pipeline.ProcessController#processChainConfig）
     */
    private final String code;
    /**
     * 业务类型说明
     */
    private final String description;
}
