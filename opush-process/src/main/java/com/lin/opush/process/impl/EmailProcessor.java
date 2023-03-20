package com.lin.opush.process.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailUtil;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.RateLimiter;

import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.domain.sms.MessageTypeConfig;
import com.lin.opush.dto.account.email.EmailAccount;
import com.lin.opush.dto.model.EmailContentModel;
import com.lin.opush.enums.ChannelType;
import com.lin.opush.enums.FlowControlStrategy;
import com.lin.opush.process.BaseProcessor;
import com.lin.opush.process.Processor;
import com.lin.opush.service.flowControl.FlowControlParam;
import com.lin.opush.utils.AccountUtils;
import com.lin.opush.utils.FileUtils;
import com.lin.opush.utils.LoadBalanceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;

/**
 * 邮件处理器
 */
@Component
@Slf4j
public class EmailProcessor extends BaseProcessor implements Processor {
    /**
     * 渠道账号工具类
     */
    @Autowired
    private AccountUtils accountUtils;

    /**
     * 流量负载工具类
     */
    @Autowired
    private LoadBalanceUtils loadBalanceUtils;

    /**
     * 文件
     */
    @Value("${opush.business.upload.crowd.path}")
    private String dataPath;

    public EmailProcessor() {
        channelTypeCode = ChannelType.EMAIL.getCode();
        // 邮件请求限流，初始限流大小为单机3qps（具体数值配置在opush.properties中调整)
        Double rateLimitInitValue = Double.valueOf(3);
        flowControlParam = FlowControlParam.builder()
                            .rateLimiter(RateLimiter.create(rateLimitInitValue))
                            .rateLimitInitValue(rateLimitInitValue)
                            .flowControlStrategy(FlowControlStrategy.REQUEST_NUM_RATE_LIMIT).build();
    }

    /**
     * 发送邮件
     * @param taskInfo 任务信息
     * @return 发送是否成功
     */
    @Override
    public boolean realSend(TaskInfo taskInfo) {
        // 邮件发送内容模型
        EmailContentModel emailContentModel = (EmailContentModel) taskInfo.getContentModel();
        // 获取不同消息类型的流量配置进行流量负载并发送邮件
        MessageTypeConfig[] messageTypeConfigs = loadBalanceUtils.loadBalance(loadBalanceUtils.getMessageTypeConfig(taskInfo));
        for (MessageTypeConfig messageTypeConfig : messageTypeConfigs) {
            // 获取邮件账号配置信息
            EmailAccount emailAccount = accountUtils.getEmailAccountBySupplierName(messageTypeConfig.getSupplierName(), EmailAccount.class);
            try {
                // 附件(读取远程链接返回File对象)
                File file = StrUtil.isNotBlank(emailContentModel.getUrl()) ? FileUtils.getRemoteUrlToFile(dataPath, emailContentModel.getUrl()) : null;
                // 判断邮件是否带附件
                String result = Objects.isNull(file) ?
                        MailUtil.send(emailAccount, taskInfo.getReceiver(), emailContentModel.getTitle(), emailContentModel.getContent(), true) :
                        MailUtil.send(emailAccount, taskInfo.getReceiver(), emailContentModel.getTitle(), emailContentModel.getContent(), true, file);
                if (StrUtil.isNotBlank(result)) {
                    return true;
                }
            } catch (Exception e) {
                log.error("EmailProcessor#realSend fail!{},params:{}", Throwables.getStackTraceAsString(e), taskInfo);
                return false;
            }
        }
        return false;
    }

    /**
     * 撤回邮件
     * @param messageTemplate 消息模板
     */
    @Override
    public void recall(MessageTemplate messageTemplate) {}
}
