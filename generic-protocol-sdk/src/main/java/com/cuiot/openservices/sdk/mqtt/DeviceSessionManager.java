package com.cuiot.openservices.sdk.mqtt;

import com.cuiot.openservices.sdk.entity.DeviceSession;
import com.cuiot.openservices.sdk.entity.response.DeviceItem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 设备会话管理
 *
 * @author unicom
 */
public final class DeviceSessionManager {

    /**
     * 设备会话池
     */
    private static final ConcurrentMap<String, DeviceSession> DEVICE_SESSION_POOL = new ConcurrentHashMap<>();

    private DeviceSessionManager() {
    }

    public static void putDeviceSession(String productKey, String deviceKey) {
        DeviceSession deviceSession = new DeviceSession(productKey, deviceKey);
        DEVICE_SESSION_POOL.put(createKey(deviceSession), deviceSession);
    }

    public static void putDeviceSession(DeviceItem deviceItem) {
        if (deviceItem == null) {
            return;
        }
        putDeviceSession(deviceItem.getProductKey(), deviceItem.getDeviceKey());
    }

    /**
     * 获取设备会话
     *
     * @param productKey 产品Key
     * @param deviceKey  设备Key
     * @return DeviceSession
     */
    public static DeviceSession getDeviceSession(String productKey, String deviceKey) {
        return DEVICE_SESSION_POOL.get(productKey + "-" + deviceKey);
    }

    public static void handleDeviceLogout(String productKey, String deviceKey) {
        // 移除
        DeviceSessionManager.removeDeviceSession(productKey, deviceKey);
    }

    public static void handleDeviceLogout(DeviceItem deviceItem) {
        // 移除
        if (deviceItem == null) {
            return;
        }
        DeviceSessionManager.removeDeviceSession(deviceItem.getProductKey(), deviceItem.getDeviceKey());
    }

    private static void removeDeviceSession(String productKey, String deviceKey) {
        DEVICE_SESSION_POOL.remove(productKey + "-" + deviceKey);
    }

    /**
     * 生成设备会话key
     *
     * @param deviceSession 设备会话
     * @return String
     */
    private static String createKey(DeviceSession deviceSession) {
        return deviceSession.getProductKey() + "-" + deviceSession.getDeviceKey();
    }
}
