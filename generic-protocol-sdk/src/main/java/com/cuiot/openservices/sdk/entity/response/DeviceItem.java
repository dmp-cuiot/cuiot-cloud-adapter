package com.cuiot.openservices.sdk.entity.response;

/**
 * @author yht
 */
public class DeviceItem {
    private String productKey;
    private String deviceKey;

    public DeviceItem() {
    }

    public DeviceItem(String productKey, String deviceKey) {
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
