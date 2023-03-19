package com.lin.opush.domain.sms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 不同消息类型【】的短信流量负载配置
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageTypeSmsConfig {
    /**
     * 权重(决定着流量的占比)
     */
    private Integer weights;

    /**
     * 短信模板若指定发送渠道账号，则该字段有值
     */
    private Integer sendAccount;

    /**
     * script名称
     */
    private String scriptName;
}
