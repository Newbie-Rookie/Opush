package com.lin.opush.service;

import com.lin.opush.domain.ChannelAccount;

import java.util.List;

/**
 * 渠道账号接口
 */
public interface IChannelAccountService {

    ChannelAccount queryByName(String name);

    List<ChannelAccount> queryByChannelType(Integer channelType);
}
