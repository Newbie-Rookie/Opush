package com.lin.opush.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.domain.MessageParam;
import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.SendRequest;
import com.lin.opush.domain.SendResponse;
import com.lin.opush.domain.sms.SmsReceipt;
import com.lin.opush.enums.BusinessCode;
import com.lin.opush.enums.RespStatusEnum;
import com.lin.opush.exception.CommonException;
import com.lin.opush.service.MessageTemplateService;
import com.lin.opush.service.RecallService;
import com.lin.opush.service.SendService;
import com.lin.opush.utils.Convert4Amis;
import com.lin.opush.vo.BasicResultVO;
import com.lin.opush.vo.MessageTemplateParam;
import com.lin.opush.vo.MessageTemplateVo;

import com.lin.opush.vo.amis.CommonAmisVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 消息模板管理控制器
 */
@RestController
@RequestMapping("/messageTemplate")
public class MessageTemplateController {
    @Autowired
    private MessageTemplateService messageTemplateService;

    @Autowired
    private SendService sendService;

    @Autowired
    private RecallService recallService;

    /**
     * 消息模板新增/修改（Id存在则修改，Id不存在则保存）
     * @param messageTemplate 消息模板信息
     * @return
     */
    @PostMapping("/save")
    public MessageTemplate saveOrUpdate(@RequestBody MessageTemplate messageTemplate) {
        return messageTemplateService.saveOrUpdate(messageTemplate);
    }

    /**
     * 获取消息模板列表数据
     * @param messageTemplateParam
     * @return
     */
    @GetMapping("/list")
    public MessageTemplateVo queryList(@Validated MessageTemplateParam messageTemplateParam) {
        Page<MessageTemplate> messageTemplates = messageTemplateService.queryList(messageTemplateParam);
        List<Map<String, Object>> result = Convert4Amis.flatListMap(messageTemplates.toList());
        return MessageTemplateVo.builder().count(messageTemplates.getTotalElements()).rows(result).build();
    }

    /**
     * 获取需要测试的模板占位符，返回给Amis
     * @param id 测试对应的消息模板id
     * @return 消息模板中是否有占位符参数
     */
    @PostMapping("/test/content")
    public CommonAmisVo test(Long id) {
        MessageTemplate messageTemplate = messageTemplateService.queryById(id);
        CommonAmisVo testContent = Convert4Amis.getTestContent(messageTemplate.getMsgContent());
        return testContent;
    }

    /**
     * 消息测试发送接口
     * @param messageTemplateParam 消息模板参数
     * @return
     */
    @PostMapping("/test")
    public SendResponse test(@RequestBody MessageTemplateParam messageTemplateParam) {
        // 将消息下发的参数信息组装为map
        Map<String, String> variables = JSON.parseObject(messageTemplateParam.getMsgContent(), Map.class);
        // 组装消息参数（消息接收者、消息下发参数）
        MessageParam messageParam = MessageParam.builder()
                                                .receiver(messageTemplateParam.getReceiver())
                                                .variables(variables).build();
        // 组装消息下发请求（业务类型：send、消息模板id、消息参数、下发者）
        SendRequest sendRequest = SendRequest.builder()
                                            .code(BusinessCode.SEND.getCode())
                                            .messageTemplateId(messageTemplateParam.getId())
                                            .messageParam(messageParam)
                                            .creator(messageTemplateParam.getCreator()).build();
        // 调用消息下发方法
        SendResponse response = sendService.send(sendRequest);
        // 根据下发情况成立
        if (!Objects.equals(response.getCode(), RespStatusEnum.SUCCESS.getCode())) {
            throw new CommonException(response.getMsg());
        }
        return response;
    }

    /**
     * 用于接收UniSMS的短信下发回执【暂时不支持内网URL，需部署到服务器才可行】
     * @param receipt UniSMS短信回执
     */
    @GetMapping("/receipt")
    public void saveReceipt(@RequestBody SmsReceipt receipt){
        messageTemplateService.saveUniSMSReceipt(receipt);
    }

    /**
     * 查找id对应消息模板
     * @param id 消息模板id
     * @return 消息模板对应Map
     */
    @GetMapping("/query/{id}")
    public Map<String, Object> queryById(@PathVariable("id") Long id) {
        return Convert4Amis.flatSingleMap(messageTemplateService.queryById(id));
    }

    /**
     * 根据id复制对应消息模板
     * @param id 消息模板id
     */
    @PostMapping("/copy/{id}")
    public void copyById(@PathVariable("id") Long id) {
        messageTemplateService.copy(id);
    }

    /**
     * 根据id进行单条删除/批量删除（多个id中间逗号隔开）
     * @param id 消息模板id
     */
    @DeleteMapping("delete/{id}")
    public void deleteByIds(@PathVariable("id") String id) {
        if (StrUtil.isNotBlank(id)) {
            // 将id字符串以,分隔开再转为流，再将每个数值转为Long类型传入map，最后转为list
            List<Long> idList = Arrays.stream(id.split(CommonConstant.COMMA)).map(Long::valueOf).collect(Collectors.toList());
            messageTemplateService.deleteByIds(idList);
        }
    }
}
