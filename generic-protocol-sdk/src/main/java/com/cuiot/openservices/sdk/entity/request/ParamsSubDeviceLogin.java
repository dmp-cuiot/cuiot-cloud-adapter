package com.cuiot.openservices.sdk.entity.request;

import com.cuiot.openservices.sdk.entity.ICheck;
import io.netty.util.internal.StringUtil;

/**
 * 请求参数params：子设备上线
 *
 * @author yht
 */
public class ParamsSubDeviceLogin implements ICheck {
    private String originalIdentity;
    private String authType;
    /**
     * 子设备产品Key
     */
    private String productKey;
    /**
     * 子设备设备Key
     */
    private String deviceKey;
    private String deviceId;
    private String signMethod;
    private String sign;
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public ParamsSubDeviceLogin() {
    }

    public String getOriginalIdentity() {
        return originalIdentity;
    }

    public void setOriginalIdentity(String originalIdentity) {
        this.originalIdentity = originalIdentity;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSignMethod() {
        return signMethod;
    }

    public void setSignMethod(String signMethod) {
        this.signMethod = signMethod;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public boolean checkLegal() {
        return !StringUtil.isNullOrEmpty(originalIdentity)
                && !StringUtil.isNullOrEmpty(authType)
                && !StringUtil.isNullOrEmpty(productKey)
                && !StringUtil.isNullOrEmpty(deviceKey)
                && !StringUtil.isNullOrEmpty(deviceId)
                && !StringUtil.isNullOrEmpty(signMethod)
                && !StringUtil.isNullOrEmpty(sign)
                && !StringUtil.isNullOrEmpty(uuid);
    }
}
