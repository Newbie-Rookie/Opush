package com.lin.opush.service;

import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.sms.SmsReceipt;
import com.lin.opush.vo.BasicResultVO;
import com.lin.opush.vo.MessageTemplateParam;
import org.springframework.data.domain.Page;

import java.io.Reader;
import java.util.List;

/**
 * 消息模板管理服务接口
 */
public interface MessageTemplateService {
    /**
     * 查询未删除的模板列表（分页)
     * @param messageTemplateParam 消息模板参数
     * @return
     */
    Page<MessageTemplate> queryList(MessageTemplateParam messageTemplateParam);

    /**
     * 消息模板新增/修改（Id存在则修改，Id不存在则保存）
     * @param messageTemplate 消息模板
     * @return 消息模板
     */
    MessageTemplate saveOrUpdate(MessageTemplate messageTemplate);

    /**
     * 根据消息模板id查询消息模板信息
     * @param id 消息模板id
     * @return 消息模板
     */
    MessageTemplate queryById(Long id);

    /**
     * 保存UniSMS短信回执
     * @param receipt UniSMS短信回执
     */
    void saveUniSMSReceipt(SmsReceipt receipt);

    /**
     * 复制id对应的消息模板
     * @param id 要复制的消息模板对应id
     */
    void copy(Long id);

    /**
     * 软删除（is_deleted = 1）
     * @param ids 单条/批量删除（删除消息模板对应id列表）
     */
    void deleteByIds(List<Long> ids);

    /**
     * 启动模板的定时任务
     * @param id 消息模板id
     * @return 处理结果
     */
    BasicResultVO startCronTask(Long id);

    /**
     * 暂停模板的定时任务
     * @param id 消息模板id
     * @return 处理结果
     */
    BasicResultVO stopCronTask(Long id);
}
