package com.cuiot.openservices.sdk.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * MessageId生成工具
 *
 * @author yht
 */
public class MessageIdUtils {

    private static final AtomicLong ID = new AtomicLong(1);

    /**
     * TODO 可以调大些
     */
    private static final Long MAX_ID = 4294967295L;

    private MessageIdUtils() {
    }

    public static String generateId() {
        long id = ID.getAndIncrement();
        // 如果超过最大值，重置
        if (id > MAX_ID) {
            // 加锁
            synchronized (MessageIdUtils.class) {
                long l = ID.get();
                // 如果未重置，重置
                if (l > MAX_ID) {
                    ID.set(1);
                    return String.valueOf(1);
                }
                // 如果已重置，重新获取
                return String.valueOf(ID.getAndIncrement());
            }
        }
        return String.valueOf(id);
    }
}
