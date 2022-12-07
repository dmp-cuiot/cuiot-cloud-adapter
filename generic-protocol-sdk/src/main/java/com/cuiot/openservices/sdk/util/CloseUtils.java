package com.cuiot.openservices.sdk.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 工具类
 *
 * @author yht
 */
public class CloseUtils {

    private static Logger logger = LoggerFactory.getLogger(CloseUtils.class);

    public static void close(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (Exception e) {
            logger.error("", e);
        }
    }

}
