package com.lin.opush.receipt;

import com.google.common.base.Throwables;
import com.lin.opush.config.SupportThreadPoolConfig;
import com.lin.opush.receipt.stater.PullReceiptStater;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 拉取回执信息入口
 */
@Component
@Slf4j
public class MessageReceipt {
    /**
     * 拉取回执启动器实现类列表
     */
    @Autowired
    private List<PullReceiptStater> pullReceiptStaterList;

    @PostConstruct
    private void init() {
        // 获取线程池执行任务【拉取回执】
        SupportThreadPoolConfig.getThreadPool().execute(() -> {
            while (true) {
                try {
                    for (PullReceiptStater pullReceiptStater : pullReceiptStaterList) {
                        pullReceiptStater.start();
                    }
                    // Thread.sleep(2000);
                } catch (Exception e) {
                    log.error("MessageReceipt#init fail:{}", Throwables.getStackTraceAsString(e));
                }
            }
        });
    }
}
