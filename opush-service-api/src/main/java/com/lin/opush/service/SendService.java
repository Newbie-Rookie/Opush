package com.lin.opush.service;

import com.lin.opush.domain.BatchSendRequest;
import com.lin.opush.domain.SendRequest;
import com.lin.opush.domain.SendResponse;

/**
 * 单条/批量消息发送接口
 */
public interface SendService {
    /**
     * 单模板单文案发送接口
     * @param sendRequest
     * @return
     */
    SendResponse send(SendRequest sendRequest);

    /**
     * 单模板多文案发送接口（减少多次远程调用）
     * @param batchSendRequest
     * @return
     */
    SendResponse batchSend(BatchSendRequest batchSendRequest);
}
