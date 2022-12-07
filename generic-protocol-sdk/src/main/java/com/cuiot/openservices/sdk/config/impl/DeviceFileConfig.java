package com.cuiot.openservices.sdk.config.impl;

import com.cuiot.openservices.sdk.config.IDeviceConfig;
import com.cuiot.openservices.sdk.constant.ConfigKey;
import com.cuiot.openservices.sdk.entity.Device;
import com.cuiot.openservices.sdk.util.CloseUtils;
import com.cuiot.openservices.sdk.util.FileConfigUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 从配置文件中读取设备参数
 *
 * @author yht
 */
public final class DeviceFileConfig implements IDeviceConfig{
    private final ReentrantLock lock = new ReentrantLock();
    private final ConcurrentMap<String, Device> configCache = new ConcurrentHashMap<>(1000);
    private final ConcurrentMap<String, String> originalIdentityCache = new ConcurrentHashMap<>(1000);
    private Logger logger = LoggerFactory.getLogger(DeviceFileConfig.class);
    private Config config;
    private String configFilePath = "config/devices.conf";
    private String proPath = "generic-protocol-maven-demo/src/main/resources/";

    public DeviceFileConfig() {
        initFileDeviceConfig();
    }

    public DeviceFileConfig(String configFilePath) {
        this.configFilePath = configFilePath;
        initFileDeviceConfig();
    }

    @Override
    public Device getDeviceEntity(String originalIdentity) {
        Device device = null;
        lock.lock();
        try {
            if (StringUtil.isNullOrEmpty(originalIdentity) || !config.hasPath(originalIdentity)) {
                logger.warn("cannot find device entity, originalIdentity:{}", originalIdentity);
                return null;
            }

            device = configCache.get(originalIdentity);
            if (device != null) {
                return device;
            }
            Config deviceConfig = config.getConfig(originalIdentity);
            String productKey = FileConfigUtils.getStringIfExists(deviceConfig, ConfigKey.PRODUCT_KEY);
            String deviceKey = FileConfigUtils.getStringIfExists(deviceConfig, ConfigKey.DEVICE_KEY);
            String deviceId = FileConfigUtils.getStringIfExists(deviceConfig, ConfigKey.DEVICE_ID);
            String deviceSecret = FileConfigUtils.getStringIfExists(deviceConfig, ConfigKey.DEVICE_SECRET);
            String signMethod = FileConfigUtils.getStringIfExists(deviceConfig, ConfigKey.SIGN_METHOD);
            String authType = FileConfigUtils.getStringIfExists(deviceConfig, ConfigKey.AUTH_TYPE);

            device = Device.builder()
                    .originalIdentity(originalIdentity)
                    .productKey(productKey)
                    .deviceId(deviceId)
                    .deviceKey(deviceKey)
                    .deviceSecret(deviceSecret)
                    .signMethod(signMethod)
                    .authType(authType)
                    .build();
            if (!device.checkLegal()) {
                logger.warn("illegal device config:{}", device);
            } else {
                configCache.put(originalIdentity, device);
                originalIdentityCache.put(productKey + "-" + deviceKey, originalIdentity);
            }
        } catch (Exception e) {
            logger.error("get device entity exception", e);
        } finally {
            lock.unlock();
        }

        return device;
    }

    @Override
    public List<Device> getAllDevices() {
        lock.lock();
        try {
            Set<Map.Entry<String, ConfigValue>> set = config.entrySet();
            HashMap<String, Device> deviceHashMap = new HashMap<>();
            set.forEach(t -> {
                String propTemp = t.getKey();
                String originalIdentity;
                String productKey = null;
                String deviceKey = null;
                if (propTemp.endsWith(".productKey")) {
                    originalIdentity = propTemp.substring(0, propTemp.length() - 11);
                    productKey = (String) t.getValue().unwrapped();
                } else if (propTemp.endsWith(".deviceKey")) {
                    originalIdentity = propTemp.substring(0, propTemp.length() - 10);
                    deviceKey = (String) t.getValue().unwrapped();
                } else {
                    return;
                }
                if (deviceHashMap.containsKey(originalIdentity)) {
                    Device device = deviceHashMap.get(originalIdentity);
                    if (productKey != null) {
                        device.setProductKey(productKey);
                    } else if (deviceKey != null) {
                        device.setDeviceKey(deviceKey);
                    }

                } else {
                    Device device = Device.builder()
                            .originalIdentity(originalIdentity)
                            .productKey(productKey)
                            .deviceKey(deviceKey)
                            .build();
                    deviceHashMap.put(originalIdentity, device);
                }
            });
            return new ArrayList<>(deviceHashMap.values());
        } catch (Exception ex) {
            logger.error("", ex);
            return null;
        } finally {
            lock.unlock();
        }


    }

    @Override
    public String getOriginalIdentity(String productKey, String deviceKey) {
        lock.lock();
        try {
            return originalIdentityCache.get(productKey + "-" + deviceKey);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean setDeviceEntity(Device device) {
        String content = "";
        FileWriter fw = null;
        try {
            //获取配置文件路径
            fw = new FileWriter(proPath + configFilePath, true);
            if (device.checkLegal()) {
                content = getContent(device);
            } else {
                logger.warn("illegal device config:{}", device);
                return false;
            }
            fw.write(content);
            return true;
        } catch (Exception e) {
            logger.error("set config by entity exception", e);
            System.out.println(e);
            return false;
        } finally {
            CloseUtils.close(fw);
        }
    }

    @Override
    public boolean setDeviceEntitys(List<Device> devices) {
        String content = "";
        FileWriter fw = null;
        try {
            fw = new FileWriter(proPath + configFilePath, true);
            for (Device device : devices) {
                if (device.checkLegal()) {
                    content = getContent(device);
                } else {
                    logger.warn("illegal device config:{}", device);
                    return false;
                }
                fw.write(content);
            }
            return true;
        } catch (Exception e) {
            logger.error("set config by entity exception", e);
            return false;
        } finally {
            CloseUtils.close(fw);
        }
    }

    public void initFileDeviceConfig() {
        this.config = ConfigFactory.load(configFilePath);
        this.config.checkValid(ConfigFactory.defaultReference());
        URL fileUrl = this.getClass().getClassLoader().getResource(configFilePath);
        if (fileUrl == null) {
            logger.error("device config file does not exist");
        }
    }

    public String getContent(Device device) {
        String content = "\r\n" + device.getOriginalIdentity() + "\t{\r\n" +
                "\t\t" + ConfigKey.PRODUCT_KEY + "\t=\t\"" + device.getProductKey() + "\"\r\n" +
                "\t\t" + ConfigKey.DEVICE_KEY + "\t=\t\"" + device.getDeviceKey() + "\"\r\n" +
                "\t\t" + ConfigKey.DEVICE_ID + "\t=\t\"" + device.getDeviceId() + "\"\r\n" +
                "\t\t" + ConfigKey.DEVICE_SECRET + "\t=\t\"" + device.getDeviceSecret() + "\"\r\n" +
                "\t\t" + ConfigKey.SIGN_METHOD + "\t=\t\"" + device.getSignMethod() + "\"\r\n" +
                "\t\t" + ConfigKey.AUTH_TYPE + "\t=\t\"" + device.getAuthType() + "\"\r\n" +
                "}";
        return content;
    }

    public String getProPath() {
        return proPath;
    }

    public void setProPath(String proPath) {
        this.proPath = proPath;
    }

    @Override
    public boolean deleteDeviceEntity(String originalIdentity, String productKey, String deviceKey) {
        return false;
    }
}
