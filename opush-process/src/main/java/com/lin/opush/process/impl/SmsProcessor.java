package com.lin.opush.process.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Throwables;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.dao.SmsRecordDao;
import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.SmsRecord;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.domain.sms.MessageTypeSmsConfig;
import com.lin.opush.domain.sms.SmsParam;
import com.lin.opush.dto.account.sms.SmsAccount;
import com.lin.opush.dto.model.SmsContentModel;
import com.lin.opush.enums.ChannelType;
import com.lin.opush.process.BaseProcessor;
import com.lin.opush.process.Processor;
import com.lin.opush.script.SmsScript;
import com.lin.opush.service.ConfigService;
import com.lin.opush.utils.AccountUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 短信处理器
 */
@Component
@Slf4j
public class SmsProcessor extends BaseProcessor implements Processor {
    /**
     * 处理器初始化（渠道类型编码）
     */
    public SmsProcessor() {
        channelTypeCode = ChannelType.SMS.getCode();
    }

    /**
     * 短信记录 Dao
     */
    @Autowired
    private SmsRecordDao smsRecordDao;

    /**
     * 配置服务
     */
    @Autowired
    private ConfigService config;

    /**
     * 自动注入SmsScript的实现类对象【TencentSmsScript...】到Map<实现类对象名,实现类对象>
     * 【只有在Map的key为String类型时才有效】
     */
    @Autowired
    private Map<String, SmsScript> smsScripts;

    /**
     * 渠道账号工具类
     */
    @Autowired
    private AccountUtils accountUtils;

    /**
     * 系统动态流量配置
     * （目前暂时支持短信和硬件动态流量配置，此时sendAccount = 0）
     */
    private static final Integer AUTO_FLOW_RULE = 0;

    /**
     * 短信不同消息类型对应动态流量配置key
     */
    private static final String AUTO_FLOW_KEY = "messageTypeSmsConfig";

    /**
     * 短信不同消息类型对应动态流量配置前缀
     */
    private static final String AUTO_FLOW_KEY_PREFIX = "message_type_";

    /**
     * 若短信含链接，则将链接拼在文案之后
     *      (1)可以考虑将链接转短链
     *      (2)若为营销类短信，需考虑拼接【回TD退订】之类的文案
     * @param taskInfo 任务信息
     * @return 短信内容
     */
    private String getSmsContent(TaskInfo taskInfo) {
        SmsContentModel smsContentModel = (SmsContentModel) taskInfo.getContentModel();
        if (StrUtil.isNotBlank(smsContentModel.getUrl())) {
            // 拼接链接
            return smsContentModel.getContent() + CommonConstant.SPACE + smsContentModel.getUrl();
        } else {
            return smsContentModel.getContent();
        }
    }

    /**
     * 获取短信渠道账号配置
     *      若模板指定具体的明确账号，则优先发其账号，否则走到系统动态流量配置（暂支持短信、邮件）
     * @param taskInfo 任务信息
     * @return 渠道账号
     */
    private List<MessageTypeSmsConfig> getMessageTypeSmsConfig(TaskInfo taskInfo) {
        // 若模板指定账号，则优先使用具体账号进行发送(sendAccount != 0)
        if (!taskInfo.getSendAccount().equals(AUTO_FLOW_RULE)) {
            // 获取sendAccount指定的短信渠道账号
            SmsAccount account = accountUtils.getAccountById(taskInfo.getSendAccount(), SmsAccount.class);
            return Arrays.asList(MessageTypeSmsConfig.builder().sendAccount(taskInfo.getSendAccount())
                                                                .scriptName(account.getScriptName())
                                                                .weights(100).build());
        }
        // 读取本地配置的对应消息类型【通知、营销、验证码】的短信流量配置
        String property = config.getProperty(AUTO_FLOW_KEY, CommonConstant.EMPTY_VALUE_JSON_ARRAY);
        JSONArray jsonArray = JSON.parseArray(property);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray array = jsonArray.getJSONObject(i).getJSONArray(AUTO_FLOW_KEY_PREFIX + taskInfo.getMsgType());
            if (CollUtil.isNotEmpty(array)) {
                return JSON.parseArray(JSON.toJSONString(array), MessageTypeSmsConfig.class);
            }
        }
        return new ArrayList<>();
    }

    /**
     * 流量负载（暂时支持短信、邮件多个渠道账号流量负载）
     *      根据配置的权重优先走某个账号，并取出一个备份的
     * @param messageTypeSmsConfigs
     * @return 选中渠道商和备选渠道商配置
     */
    private MessageTypeSmsConfig[] loadBalance(List<MessageTypeSmsConfig> messageTypeSmsConfigs) {
        int total = 0;
        for (MessageTypeSmsConfig channelConfig : messageTypeSmsConfigs) {
            total += channelConfig.getWeights();
        }
        // 生成一个[1,total]区间内的随机数，根据该值落在哪个区间 → 选择渠道商
        Random random = new Random();
        int index = random.nextInt(total) + 1;
        // 选中渠道商
        MessageTypeSmsConfig pitchOnSupplier;
        // 备选渠道商
        MessageTypeSmsConfig alterNativeSupplier;
        for (int i = 0; i < messageTypeSmsConfigs.size(); ++i) {
            if (index <= messageTypeSmsConfigs.get(i).getWeights()) {
                pitchOnSupplier = messageTypeSmsConfigs.get(i);
                // 取下一个渠道商作为备选渠道商
                int j = (i + 1) % messageTypeSmsConfigs.size();
                // 仅一个渠道商
                if (i == j) {
                    return new MessageTypeSmsConfig[]{pitchOnSupplier};
                }
                alterNativeSupplier = messageTypeSmsConfigs.get(j);
                return new MessageTypeSmsConfig[]{pitchOnSupplier, alterNativeSupplier};
            }
            // 删除前一个渠道商的权重(否则可能无法选中渠道商)
            index -= messageTypeSmsConfigs.get(i).getWeights();
        }
        return null;
    }

    /**
     * 发送短信
     * @param taskInfo 任务信息
     * @return 发送是否成功
     */
    @Override
    public boolean realSend(TaskInfo taskInfo) {
        // 组装发送短信参数【发送手机号、发送内容、消息模板Id】
        SmsParam smsParam = SmsParam.builder().phones(taskInfo.getReceiver()).content(getSmsContent(taskInfo))
                                                .messageTemplateId(taskInfo.getMessageTemplateId()).build();
        try {
            // 获取不同消息类型的流量配置进行流量负载并发送短信
            MessageTypeSmsConfig[] messageTypeSmsConfigs = loadBalance(getMessageTypeSmsConfig(taskInfo));
            for (MessageTypeSmsConfig messageTypeSmsConfig : messageTypeSmsConfigs) {
                smsParam.setScriptName(messageTypeSmsConfig.getScriptName());
                smsParam.setSendAccountId(messageTypeSmsConfig.getSendAccount());
                // 短信下发记录列表
                List<SmsRecord> recordList = smsScripts.get(messageTypeSmsConfig.getScriptName()).send(smsParam);
                if (CollUtil.isNotEmpty(recordList)) {
                    // 短信记录持久化
                    smsRecordDao.saveAll(recordList);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("SmsProcessor#realSend fail:{},params:{}", Throwables.getStackTraceAsString(e), JSON.toJSONString(smsParam));
        }
        return false;
    }

    /**
     * 撤回短信
     * @param messageTemplate 消息模板
     */
    @Override
    public void recall(MessageTemplate messageTemplate) {
    }
}
