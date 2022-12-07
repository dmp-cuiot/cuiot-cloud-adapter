package com.cuiot.openservices.sdk.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HamSha256加密
 *
 * @author wuyiyu
 */
public class HmaSha256 {
    private static Logger logger = LoggerFactory.getLogger(HmaSha256.class);

    /**
     * 将加密后的字节数组转换成字符串
     *
     * @param b 字节数组
     * @return String
     */
    public static String byteArrayToHexString(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String temp;
        for (int n = 0; b != null && n < b.length; n++) {
            temp = Integer.toHexString(b[n] & 0XFF);
            if (temp.length() == 1) {
                hs.append('0');
            }
            hs.append(temp);
        }
        return hs.toString().toLowerCase();
    }

    /**
     * sha256_HMAC加密
     *
     * @param message 消息
     * @param secret  秘钥
     * @return String
     */
    public static String sha256Hmac(String message, String secret) {
        String hash = "";
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] bytes = sha256Hmac.doFinal(message.getBytes());
            hash = byteArrayToHexString(bytes);
        } catch (Exception e) {
            logger.error("Error HmacSHA256", e);
        }
        return hash;
    }

}
