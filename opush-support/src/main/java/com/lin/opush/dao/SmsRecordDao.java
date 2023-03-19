package com.lin.opush.dao;

import com.lin.opush.domain.SmsRecord;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * 短信记录 Dao
 */
public interface SmsRecordDao extends CrudRepository<SmsRecord, Long> {
    /**
     * 根据手机号和日期获取短信下发记录
     * @param phone 手机号
     * @param sendDate 日期
     * @return 短信下发记录
     */
    List<SmsRecord> findByPhoneEqualsAndSendDateEquals(Long phone, Integer sendDate);
}
