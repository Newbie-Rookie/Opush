package com.lin.opush.service.consume;

import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.TaskInfo;

import java.util.List;

/**
 * 消费消息服务（发送 / 撤回）
 */
public interface ConsumeService {
    /**
     * 从消息拉到消息进行消费，发送消息
     * @param taskInfoLists 任务信息列表
     */
    void consumeToSend(List<TaskInfo> taskInfoLists);

    /**
     * 从MQ拉到消息进行消费，撤回消息
     * @param messageTemplate 消息模板
     */
    void consumeToRecall(MessageTemplate messageTemplate);
}
