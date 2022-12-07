package com.cuiot.openservices.sdk.entity;

import com.cuiot.openservices.sdk.constant.CommonConstant;
import com.cuiot.openservices.sdk.util.HmaSha256;
import com.cuiot.openservices.sdk.util.Sm3Util;
import io.netty.util.internal.StringUtil;

/**
 * 设备
 *
 * @author yht
 */
public class Device implements ICheck {
    /**
     * 用户云平台真实设备的唯一标识
     */
    private String originalIdentity;
    /**
     * DMP云平台设备信息
     */
    private String productKey;
    private String deviceKey;
    private String deviceSecret;
    private String deviceId;
    private String signMethod = CommonConstant.SIGN_METHOD_ONE;
    private String authType = CommonConstant.AUTH_TYPE_ZERO;

    public String getOriginalIdentity() {
        return originalIdentity;
    }

    public void setOriginalIdentity(String originalIdentity) {
        this.originalIdentity = originalIdentity;
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

    public String getDeviceSecret() {
        return deviceSecret;
    }

    public void setDeviceSecret(String deviceSecret) {
        this.deviceSecret = deviceSecret;
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

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    /**
     * 设备上线参数: clientId
     *
     * @return String
     */
    public String getClientId() {
        String operator = CommonConstant.OPERATOR_ZERO;
        return deviceId + "|" + productKey + "|" + signMethod + "|" + authType + "|" + operator;
    }

    /**
     * 设备上线参数: username
     *
     * @return String
     */
    public String getUsername() {
        return deviceKey + "|" + productKey;
    }

    /**
     * 设备上线参数: password
     *
     * @return String
     */
    public String getPassword() {
        if (CommonConstant.SIGN_METHOD_ZERO.equals(signMethod)) {
            return HmaSha256.sha256Hmac(deviceId + deviceKey + productKey, deviceSecret);
        } else {
            return Sm3Util.plainEncrypt(deviceSecret, deviceId + deviceKey + productKey);
        }
    }

    /**
     * 校验成员变量是否合法：所有成员变量非空
     *
     * @return boolean
     */
    @Override
    public boolean checkLegal() {
        return !StringUtil.isNullOrEmpty(originalIdentity)
                && !StringUtil.isNullOrEmpty(productKey)
                && !StringUtil.isNullOrEmpty(deviceKey)
                && !StringUtil.isNullOrEmpty(deviceSecret)
                && !StringUtil.isNullOrEmpty(deviceId)
                && !StringUtil.isNullOrEmpty(signMethod)
                && !StringUtil.isNullOrEmpty(authType);
    }

    public static boolean checkLegal(Device device) {
        return device != null && device.checkLegal();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String originalIdentity;
        private String productKey;
        private String deviceKey;
        private String deviceSecret;
        private String deviceId;
        private String signMethod = CommonConstant.SIGN_METHOD_ONE;
        private String authType = CommonConstant.AUTH_TYPE_ZERO;

        private Builder() {
        }

        public Builder originalIdentity(String originalIdentity) {
            this.originalIdentity = originalIdentity;
            return this;
        }

        public Builder productKey(String productKey) {
            this.productKey = productKey;
            return this;
        }

        public Builder deviceKey(String deviceKey) {
            this.deviceKey = deviceKey;
            return this;
        }

        public Builder deviceSecret(String deviceSecret) {
            this.deviceSecret = deviceSecret;
            return this;
        }

        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder signMethod(String signMethod) {
            this.signMethod = signMethod;
            return this;
        }

        public Builder authType(String authType) {
            this.authType = authType;
            return this;
        }

        public Device build() {
            Device device = new Device();
            device.setOriginalIdentity(originalIdentity);
            device.setProductKey(productKey);
            device.setDeviceKey(deviceKey);
            device.setDeviceSecret(deviceSecret);
            device.setDeviceId(deviceId);
            device.setSignMethod(signMethod);
            device.setAuthType(authType);
            return device;
        }
    }

    @Override
    public String toString() {
        return "Device{" +
                "originalIdentity='" + originalIdentity + '\'' +
                ", productKey='" + productKey + '\'' +
                ", deviceKey='" + deviceKey + '\'' +
                ", deviceSecret='" + deviceSecret + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", signMethod='" + signMethod + '\'' +
                ", authType='" + authType + '\'' +
                '}';
    }
}
