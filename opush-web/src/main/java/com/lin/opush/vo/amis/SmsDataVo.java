package com.lin.opush.vo.amis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 短信下发记录对应VO【适配Amis前端】
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SmsDataVo {
    /**
     * items
     */
    private List<ItemsVO> items;

    /**
     * ItemsVO
     */
    @Data
    @Builder
    public static class ItemsVO {
        /**
         * 业务ID【模板Id】
         */
        private String businessId;

        /**
         * 渠道商名
         */
        private String supplierName;

        /**
         * 接收者【手机号】
         */
        private Long phone;

        /**
         * 发送内容
         */
        private String content;

        /**
         * 发送状态
         */
        private String sendType;

        /**
         * 回执状态
         */
        private String receiveType;

        /**
         * 回执报告
         */
        private String receiveContent;

        /**
         * 发送时间
         */
        private String sendTime;

        /**
         * 回执时间
         */
        private String receiveTime;
    }
}
