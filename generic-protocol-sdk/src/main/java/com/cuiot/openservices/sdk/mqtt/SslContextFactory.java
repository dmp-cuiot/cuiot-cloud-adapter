package com.cuiot.openservices.sdk.mqtt;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.InputStream;

/**
 * @author unicom
 */
public final class SslContextFactory {
    private static Logger logger = LoggerFactory.getLogger(SslContextFactory.class);

    public static SslContext createSslContext() {
        SslContext sslContext = null;
        InputStream resourceAsStream = SslContextFactory.class.getResourceAsStream("/cuiot.cer");
        try {
            sslContext = SslContextBuilder.forClient().trustManager(resourceAsStream).build();
        } catch (SSLException e) {
            logger.error("create ssl context exception", e);
        }
        return sslContext;
    }
}
