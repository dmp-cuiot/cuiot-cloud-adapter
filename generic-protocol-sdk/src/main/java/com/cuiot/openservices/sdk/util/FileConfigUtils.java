package com.cuiot.openservices.sdk.util;

import com.typesafe.config.Config;

/**
 * 文件配置工具类
 *
 * @author unicom
 */
public final class FileConfigUtils {

    /**
     * 从配置文件中读取字符串
     *
     * @param config 配置
     * @param path   路径
     * @return 字符串，如果路径不存在返回null
     */
    public static String getStringIfExists(Config config, String path) {
        return (config != null && config.hasPath(path)) ? config.getString(path) : null;
    }

    /**
     * 从配置文件中读取Integer
     *
     * @param config 配置
     * @param path   路径
     * @return Integer，如果路径不存在返回null
     */
    public static Integer getIntegerIfExists(Config config, String path) {
        return (config != null && config.hasPath(path)) ? config.getInt(path) : null;
    }

    /**
     * 从配置文件中读取Long
     *
     * @param config 配置
     * @param path   路径
     * @return Long，如果路径不存在返回null
     */
    public static Long getLongIfExists(Config config, String path) {
        return (config != null && config.hasPath(path)) ? config.getLong(path) : null;
    }

    /**
     * 从配置文件中读取Boolean
     *
     * @param config 配置
     * @param path   路径
     * @return Boolean，如果路径不存在返回null
     */
    public static Boolean getBooleanIfExists(Config config, String path) {
        return (config != null && config.hasPath(path)) ? config.getBoolean(path) : null;
    }

}
