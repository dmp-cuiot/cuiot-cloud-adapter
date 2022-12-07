package com.cuiot.openservices.sdk.entity.response;

import com.cuiot.openservices.sdk.entity.Device;

/**
 * 响应类：子设备响应平台命令，包含设置属性，调用服务
 *
 * @author yht
 */
public class SubDeviceCmdResponse<T> extends Response<T> {

    private String productKey;
    private String deviceKey;

    public SubDeviceCmdResponse() {
    }

    public SubDeviceCmdResponse(String messageId, String code, String message, Device subDevice) {
        super(messageId, code, message);
        this.productKey = subDevice.getProductKey();
        this.deviceKey = subDevice.getDeviceKey();
    }

    public SubDeviceCmdResponse(String messageId, String code, String message, Device subDevice, T data) {
        super(messageId, code, message, data);
        this.productKey = subDevice.getProductKey();
        this.deviceKey = subDevice.getDeviceKey();
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
