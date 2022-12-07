package com.cuiot.openservices.sdk.entity;

/**
 * 设备连接Session
 *
 * @author yht
 */
public final class DeviceSession {
    /**
     * 产品Key
     */
    private String productKey;
    /**
     * 设备Key
     */
    private String deviceKey;

    public DeviceSession() {
    }

    public DeviceSession(String productKey, String deviceKey) {
        this.productKey = productKey;
        this.deviceKey = deviceKey;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

}
