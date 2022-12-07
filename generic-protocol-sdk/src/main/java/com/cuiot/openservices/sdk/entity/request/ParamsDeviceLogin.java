package com.cuiot.openservices.sdk.entity.request;

import com.cuiot.openservices.sdk.entity.ICheck;
import io.netty.util.internal.StringUtil;

/**
 * 请求参数params：直连设备上线
 *
 * @author yht
 */
public class ParamsDeviceLogin implements ICheck {
    private String originalIdentity;
    private String authType;
    private String operator;
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

    public ParamsDeviceLogin() {
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

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
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
                && !StringUtil.isNullOrEmpty(operator)
                && !StringUtil.isNullOrEmpty(deviceId)
                && !StringUtil.isNullOrEmpty(signMethod)
                && !StringUtil.isNullOrEmpty(sign)
                && !StringUtil.isNullOrEmpty(uuid);
    }
}
