package com.cuiot.openservices.sdk.constant;

/**
 * @author unicom
 */
public final class CommonConstant {
    /**
     * topic使用/分割后，数组最短长度
     */
    public static final int TOPIC_SPLIT_MIN_LENGTH = 5;

    /**
     * 签名方法
     */
    public static final String SIGN_METHOD_ZERO = "0";
    public static final String SIGN_METHOD_ONE = "1";

    /**
     * 认证方式：一机一密、泛协议服务
     */
    public static final String AUTH_TYPE_ZERO = "0";
    public static final String AUTH_TYPE_THREE = "3";

    /**
     * operator
     */
    public static final String OPERATOR_ZERO = "0";

    /**
     * 单位秒
     */
    public static final int MQTT_KEEPALIVE_TIME = 60;

    public static final String PRODUCT_KEY = "productKey";
    public static final String DEVICE_KEY = "deviceKey";
    public static final String ORIGINAL_IDENTITY = "originalIdentity";

    /**
     * dmp平台，mqtt编解码字符集
     */
    public static final String MQTT_CODEC_DMP = "GBK";

    /**
     * SDK缓存中间消息时间，单位秒
     */
    public static final int CACHE_MESSAGE_TIME = 60;

    /**
     * 默认处理上下行消息线程数
     */
    public static final int DEFAULT_HANDLER_THREADS = 2;

    /**
     * 静态conf文件配置
     */
    public static final String CONF_CONFIG = "0";

    /**
     * 单机动态缓存配置
     */
    public static final String STANDALONE_REDIS_CONFIG = "1";

    /**
     * 集群版缓存配置
     */
    public static final String CLUSTER_REDIS_CONFIG = "2";
}
