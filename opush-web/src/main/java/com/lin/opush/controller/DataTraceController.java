package com.lin.opush.controller;

import cn.hutool.core.util.StrUtil;
import com.lin.opush.service.DataTraceService;
import com.lin.opush.vo.DataTraceParam;
import com.lin.opush.vo.amis.SmsDataVo;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 数据全链路追踪控制器
 */
@Slf4j
@RestController
@RequestMapping("/trace")
public class DataTraceController {
    @Autowired
    private DataTraceService dataTraceService;

    /**
     * 获取短信下发记录
     * @param dataTraceParam 数据全链路追踪请求参数
     * @return 短信下发记录对应VO
     */
    @PostMapping("/sms")
    public SmsDataVo getSmsData(@RequestBody DataTraceParam dataTraceParam) {
        if (Objects.isNull(dataTraceParam) ||
                Objects.isNull(dataTraceParam.getDateTime()) ||
                StrUtil.isBlank(dataTraceParam.getReceiver())) {
            // 返回内容为空的SmsDataVo
            return SmsDataVo.builder().items(Lists.newArrayList()).build();
        }
        return dataTraceService.getSmsDataTrace(dataTraceParam);
    }

//    @PostMapping("/user")
//    @ApiOperation("/获取【当天】用户接收消息的全链路数据")
//    public UserTimeLineVo getUserData(@RequestBody DataParam dataParam) {
//        return dataService.getTraceUserInfo(dataParam.getReceiver());
//    }

//    @PostMapping("/messageTemplate")
//    @ApiOperation("/获取消息模板全链路数据")
//    public EchartsVo getMessageTemplateData(@RequestBody DataParam dataParam) {
//        EchartsVo echartsVo = EchartsVo.builder().build();
//        if (StrUtil.isNotBlank(dataParam.getBusinessId())) {
//            echartsVo = dataService.getTraceMessageTemplateInfo(dataParam.getBusinessId());
//        }
//        return echartsVo;
//    }
}
