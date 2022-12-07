package com.cuiot.openservices.sdk.util;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dengmawei
 **/
public class Sm3Util {
    private static Logger logger = LoggerFactory.getLogger(Sm3Util.class);

    private static final String CHARSET = "UTF-8";

    /**
     * 16进制字符串SM3生成HASH签名值算法
     *
     * @param hexString 16进制字符串
     * @return String
     */
    public static String hexEncrypt(String hexString) {
        byte[] srcData = Hex.decode(hexString);
        byte[] encrypt = encrypt(srcData);
        String cipherStr = Hex.toHexString(encrypt);
        return cipherStr;
    }

    /**
     * 16进制字符串SM3生成HASH签名值算法
     *
     * @param hexKey    16进制密钥
     * @param hexString 16进制字符串
     * @return String
     */
    public static String hexEncrypt(String hexKey, String hexString) {
        byte[] key = Hex.decode(hexKey);
        byte[] srcData = Hex.decode(hexString);
        byte[] encrypt = encrypt(key, srcData);
        String cipherStr = Hex.toHexString(encrypt);
        return cipherStr;
    }

    /**
     * 普通文本SM3生成HASH签名算法
     *
     * @param plain 待签名数据
     * @return String
     */
    public static String plainEncrypt(String plain) {
        // 将返回的hash值转换成16进制字符串
        String cipherStr = null;
        try {
            //将字符串转换成byte数组
            byte[] srcData = plain.getBytes(CHARSET);
            //调用encrypt计算hash
            byte[] encrypt = encrypt(srcData);
            //将返回的hash值转换成16进制字符串
            cipherStr = Hex.toHexString(encrypt);
        } catch (Exception e) {
            logger.error("将字符串转换为字节时出现异常", e);
        }
        return cipherStr;
    }

    /**
     * 普通文本SM3生成HASH签名算法
     *
     * @param hexKey 密钥
     * @param plain  待签名数据
     * @return String
     */
    public static String plainEncrypt(String hexKey, String plain) {
        // 将返回的hash值转换成16进制字符串
        String cipherStr = null;
        try {
            //将字符串转换成byte数组
            byte[] srcData = plain.getBytes(CHARSET);
            //密钥
            byte[] key = Hex.decode(hexKey);
            //调用encrypt计算hash
            byte[] encrypt = encrypt(key, srcData);
            //将返回的hash值转换成16进制字符串
            cipherStr = Hex.toHexString(encrypt);
        } catch (Exception e) {
            logger.error("将字符串转换为字节时出现异常", e);
        }
        return cipherStr;
    }

    /**
     * SM3计算hashCode
     *
     * @param srcData 待计算数据
     * @return byte[]
     */
    public static byte[] encrypt(byte[] srcData) {
        SM3Digest sm3Digest = new SM3Digest();
        sm3Digest.update(srcData, 0, srcData.length);
        byte[] encrypt = new byte[sm3Digest.getDigestSize()];
        sm3Digest.doFinal(encrypt, 0);
        return encrypt;
    }

    /**
     * 通过密钥进行加密
     *
     * @param key     密钥byte数组
     * @param srcData 被加密的byte数组
     * @return byte[]
     */
    public static byte[] encrypt(byte[] key, byte[] srcData) {
        KeyParameter keyParameter = new KeyParameter(key);
        SM3Digest digest = new SM3Digest();
        HMac mac = new HMac(digest);
        mac.init(keyParameter);
        mac.update(srcData, 0, srcData.length);
        byte[] result = new byte[mac.getMacSize()];
        mac.doFinal(result, 0);
        return result;
    }
}
