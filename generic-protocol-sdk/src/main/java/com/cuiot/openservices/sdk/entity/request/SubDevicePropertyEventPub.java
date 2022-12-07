package com.cuiot.openservices.sdk.entity.request;

import com.cuiot.openservices.sdk.entity.ICheck;
import com.cuiot.openservices.sdk.util.Utils;

import java.util.List;

/**
 * @author zhh
 */
public class SubDevicePropertyEventPub implements ICheck {

    private List<ParamsSubDevicePropertyEventPub> subDevices;

    public List<ParamsSubDevicePropertyEventPub> getSubDevices() {
        return subDevices;
    }

    private String uuid;

    public void setSubDevices(List<ParamsSubDevicePropertyEventPub> subDevices) {
        this.subDevices = subDevices;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setSubDevicePubs(List<ParamsSubDevicePropertyEventPub> subDevices) {
        this.subDevices = subDevices;
    }

    @Override
    public boolean checkLegal() {
        if (Utils.isEmpty(subDevices)) {
            return false;
        }
        for (ParamsSubDevicePropertyEventPub item : subDevices) {
            if (!item.checkLegal()) {
                return false;
            }
        }
        return true;
    }
}
