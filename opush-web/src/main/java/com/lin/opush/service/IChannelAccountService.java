package com.lin.opush.service;

import com.lin.opush.domain.ChannelAccount;

import java.util.List;

public interface IChannelAccountService {

    ChannelAccount queryByName(String name);

    List<ChannelAccount> queryByChannelType(Integer channelType);
}
