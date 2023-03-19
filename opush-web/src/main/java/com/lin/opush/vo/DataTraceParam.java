package com.lin.opush.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据全链路追踪请求参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataTraceParam {
    /**
     * 查看用户的链路信息
     */
    private String receiver;

    /**
     * 业务Id【数据追踪使用】
     * 生成逻辑：com.lin.opush.utils.TaskInfoUtils#generateBusinessId
     */
    private String businessId;

    /**
     * 日期时间【检索短信的条件使用】
     */
    private Long dateTime;
}
