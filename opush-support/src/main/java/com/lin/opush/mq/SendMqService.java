package com.lin.opush.mq;

/**
 * 发送数据至消息队列
 */
public interface SendMqService {
    /**
     * 发送消息
     * @param topic
     * @param jsonValue
     * @param tagId
     */
    void send(String topic, String jsonValue, String tagId);

    /**
     * 发送消息
     * @param topic
     * @param jsonValue
     */
    void send(String topic, String jsonValue);
}
