package com.cuiot.openservices.sdk.entity.request;

import com.cuiot.openservices.sdk.entity.ICheck;
import com.cuiot.openservices.sdk.util.Utils;

import java.util.List;

/**
 * @author zhh
 */
public class SubDeviceTopoUpdate implements ICheck {

    private List<SubDeviceTopoUpdateParams> subDevices;

    public List<SubDeviceTopoUpdateParams> getSubDevices() {
        return subDevices;
    }
    public void setSubDevices(List<SubDeviceTopoUpdateParams> subDevices) {
        this.subDevices = subDevices;
    }

    @Override
    public boolean checkLegal() {
        if (Utils.isEmpty(subDevices)) {
            return false;
        }
        for (SubDeviceTopoUpdateParams item : subDevices) {
            if (!item.checkLegal()) {
                return false;
            }
        }
        return true;
    }

    public static class SubDeviceTopoUpdateParams implements ICheck {
        private String uuid;

        private String productKey;
        private String deviceKey;
        private String deviceId;

        private String signMethod;
        private String sign;
        private String authType;

        public String getAuthType() {
            return authType;
        }

        public void setAuthType(String authType) {
            this.authType = authType;
        }

        private String originalIdentity;

        public String getDeviceKey() {
            return deviceKey;
        }

        public void setDeviceKey(String deviceKey) {
            this.deviceKey = deviceKey;
        }

        public String getOriginalIdentity() {
            return originalIdentity;
        }

        public void setOriginalIdentity(String originalIdentity) {
            this.originalIdentity = originalIdentity;
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

        public String getProductKey() {
            return productKey;
        }

        public void setProductKey(String productKey) {
            this.productKey = productKey;
        }


        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        @Override
        public boolean checkLegal() {
            return !Utils.isEmpty(originalIdentity);
        }
    }
}
