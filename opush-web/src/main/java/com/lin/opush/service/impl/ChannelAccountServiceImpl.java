package com.lin.opush.service.impl;

import com.lin.opush.dao.ChannelAccountDao;
import com.lin.opush.domain.ChannelAccount;
import com.lin.opush.service.IChannelAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelAccountServiceImpl implements IChannelAccountService {

    @Autowired
    private ChannelAccountDao channelAccountDao;

    @Override
    public ChannelAccount queryByName(String name){
        return channelAccountDao.findByNameEquals(name);
    }

    @Override
    public List<ChannelAccount> queryByChannelType(Integer channelType) {
        return channelAccountDao.findAllBySendChannelEquals(channelType);
    }
}
