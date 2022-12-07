package com.cuiot.openservices.sdk.entity;

import com.cuiot.openservices.sdk.constant.CommonConstant;
import com.cuiot.openservices.sdk.util.HmaSha256;
import com.cuiot.openservices.sdk.util.Sm3Util;
import com.cuiot.openservices.sdk.util.Utils;

import java.util.UUID;

/**
 * 泛协议服务
 *
 * @author yht
 */
public class Service implements ICheck {
    /**
     * DMP云平台设备信息
     */
    private String serviceId;
    private String serviceKey;
    private String serviceSecret;
    private String uuid;
    private String signMethod = CommonConstant.SIGN_METHOD_ONE;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getServiceSecret() {
        return serviceSecret;
    }

    public void setServiceSecret(String serviceSecret) {
        this.serviceSecret = serviceSecret;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSignMethod() {
        return signMethod;
    }

    public void setSignMethod(String signMethod) {
        this.signMethod = signMethod;
    }

    /**
     * 设备上线参数: clientId
     *
     * @return String
     */
    public String getClientId() {
        String authType = CommonConstant.AUTH_TYPE_THREE;
        String operator = CommonConstant.OPERATOR_ZERO;
        //补充UUID，适应多实例部署方式
        return serviceId + "|" + serviceKey + "|" + signMethod + "|" + authType + "|" + operator + "|" + uuid;
    }

    /**
     * 设备上线参数: username
     *
     * @return String
     */
    public String getUsername() {
        return serviceKey;
    }

    /**
     * 设备上线参数: password
     *
     * @return String
     */
    public String getPassword() {
        if (CommonConstant.SIGN_METHOD_ZERO.equals(signMethod)) {
            return HmaSha256.sha256Hmac(serviceId + serviceKey, serviceSecret);
        } else {
            return Sm3Util.plainEncrypt(serviceSecret, serviceId + serviceKey);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean checkLegal() {
        return Utils.isNotEmpty(serviceId) && Utils.isNotEmpty(serviceKey)
                && Utils.isNotEmpty(serviceSecret) && Utils.isNotEmpty(signMethod);
    }

    public static final class Builder {
        private String serviceId;
        private String serviceKey;
        private String serviceSecret;
        private String signMethod = CommonConstant.SIGN_METHOD_ONE;

        private Builder() {
        }

        public Builder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public Builder serviceKey(String serviceKey) {
            this.serviceKey = serviceKey;
            return this;
        }

        public Builder serviceSecret(String serviceSecret) {
            this.serviceSecret = serviceSecret;
            return this;
        }

        public Builder signMethod(String signMethod) {
            this.signMethod = signMethod;
            return this;
        }

        public Service build() {
            Service service = new Service();
            service.setServiceId(serviceId);
            service.setServiceKey(serviceKey);
            service.setServiceSecret(serviceSecret);
            service.setSignMethod(signMethod);
            service.setUuid(UUID.randomUUID().toString());
            return service;
        }
    }
}
