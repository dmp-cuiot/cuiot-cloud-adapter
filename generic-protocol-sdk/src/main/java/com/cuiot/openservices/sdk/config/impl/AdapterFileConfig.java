package com.cuiot.openservices.sdk.config.impl;

import com.cuiot.openservices.sdk.config.IAdapterConfig;
import com.cuiot.openservices.sdk.constant.CommonConstant;
import com.cuiot.openservices.sdk.constant.ConfigKey;
import com.cuiot.openservices.sdk.util.FileConfigUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.netty.util.internal.StringUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 从配置文件中读取泛协议服务参数
 *
 * @author yht
 */
public final class AdapterFileConfig implements IAdapterConfig {

    private volatile Config config;

    private String configFilePath = "config/adapter.conf";

    private final ConcurrentMap<String, Object> configCache = new ConcurrentHashMap<>(100);

    public AdapterFileConfig() {
        initFileAdapterConfig();
    }

    public AdapterFileConfig(String configFilePath) {
        this.configFilePath = configFilePath;
        initFileAdapterConfig();
    }

    @Override
    public String getConnectionHost() {
        return getString(ConfigKey.CONNECTION_HOST);
    }

    @Override
    public int getConnectionPort() {
        return getInteger(ConfigKey.CONNECTION_PORT);
    }

    @Override
    public String getServiceId() {
        return getString(ConfigKey.ADAPTER_SERVICE_ID);
    }

    @Override
    public String getServiceKey() {
        return getString(ConfigKey.ADAPTER_SERVICE_KEY);
    }

    @Override
    public String getServiceSecret() {
        return getString(ConfigKey.ADAPTER_SERVICE_SECRET);
    }

    @Override
    public String getSignMethod() {
        return getString(ConfigKey.ADAPTER_SIGN_METHOD);
    }

    @Override
    public Boolean enableTls() {
        return getBoolean(ConfigKey.ADAPTER_ENABLE_TLS);
    }

    @Override
    public Boolean enableReconnect() {
        return getBoolean(ConfigKey.ENABLE_RECONNECT);
    }

    @Override
    public Long getReconnectInterval() {
        return getLong(ConfigKey.RECONNECT_INTERVAL);
    }

    @Override
    public Long getMaxReconnectInterval() {
        return getLong(ConfigKey.MAX_RECONNECT_INTERVAL);
    }

    @Override
    public String getRedisServer() {
        return getString("redisServer");
    }

    @Override
    public String getRedisPd() {
        return getString("redisPd");
    }

    @Override
    public String getConfigType() {
        return getString("configType");
    }

    @Override
    public Integer getHandlerThreads() {
        Integer value = getInteger(ConfigKey.HANDLER_THREADS);
        return value != null ? value : CommonConstant.DEFAULT_HANDLER_THREADS;
    }

    private String getString(String name) {
        if (configCache.containsKey(name)) {
            return (String) configCache.get(name);
        }

        String value = FileConfigUtils.getStringIfExists(config, name);
        if (!StringUtil.isNullOrEmpty(value)) {
            configCache.put(name, value);
        }
        return value;
    }

    private Boolean getBoolean(String name) {
        if (configCache.containsKey(name)) {
            return (Boolean) configCache.get(name);
        }
        Boolean value = FileConfigUtils.getBooleanIfExists(config, name);
        if (value != null) {
            configCache.put(name, value);
        }
        return value;
    }

    private Integer getInteger(String name) {
        if (configCache.containsKey(name)) {
            return (Integer) configCache.get(name);
        }
        Integer value = FileConfigUtils.getIntegerIfExists(config, name);
        if (value != null) {
            configCache.put(name, value);
        }
        return value;
    }

    private Long getLong(String name) {
        if (configCache.containsKey(name)) {
            return (Long) configCache.get(name);
        }
        Long value = FileConfigUtils.getLongIfExists(config, name);
        if (value != null) {
            configCache.put(name, value);
        }
        return value;
    }

    private void initFileAdapterConfig() {
        configCache.clear();
        config = ConfigFactory.load(configFilePath);
        config.checkValid(ConfigFactory.defaultReference());
    }

}
