package com.cuiot.openservices.sdk.entity.request;

import com.cuiot.openservices.sdk.entity.ICheck;
import io.netty.util.internal.StringUtil;

/**
 * 请求参数params：子设备下线
 *
 * @author yht
 */
public class ParamsSubDeviceLogout implements ICheck {
    /**
     * 子设备产品Key
     */
    private String productKey;
    /**
     * 子设备设备Key
     */
    private String deviceKey;

    public ParamsSubDeviceLogout() {
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

    @Override
    public boolean checkLegal() {
        return !StringUtil.isNullOrEmpty(productKey)
                && !StringUtil.isNullOrEmpty(deviceKey);
    }
}
