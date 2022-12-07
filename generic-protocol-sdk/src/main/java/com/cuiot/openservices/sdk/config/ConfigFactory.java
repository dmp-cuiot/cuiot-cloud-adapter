package com.cuiot.openservices.sdk.config;

/**
 * 全局配置类，非线程安全
 *
 * @author yht
 */
public final class ConfigFactory {
    private static IAdapterConfig adapterConfig;
    private static IDeviceConfig deviceConfig;

    /**
     * 初始化配置
     *
     * @param adapterConfig 泛协议服务配置
     * @param deviceConfig  设备配置
     */
    public static void init(IAdapterConfig adapterConfig, IDeviceConfig deviceConfig) {
        ConfigFactory.adapterConfig = adapterConfig;
        ConfigFactory.deviceConfig = deviceConfig;
    }

    public static IAdapterConfig getAdapterConfig() {
        return adapterConfig;
    }

    public static IDeviceConfig getDeviceConfig() {
        return deviceConfig;
    }
}
