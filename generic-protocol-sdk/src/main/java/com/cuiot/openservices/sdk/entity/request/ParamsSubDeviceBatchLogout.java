package com.cuiot.openservices.sdk.entity.request;

import com.cuiot.openservices.sdk.entity.ICheck;
import com.cuiot.openservices.sdk.util.CheckArrayList;
import com.cuiot.openservices.sdk.util.Utils;

/**
 * 请求参数params：子设备批量下线
 *
 * @author yht
 */
public class ParamsSubDeviceBatchLogout implements ICheck {
    private CheckArrayList<ParamsSubDeviceLogout> subDevices = new CheckArrayList<>();

    public ParamsSubDeviceBatchLogout() {
    }

    public CheckArrayList<ParamsSubDeviceLogout> getSubDevices() {
        return subDevices;
    }

    public void setSubDevices(CheckArrayList<ParamsSubDeviceLogout> subDevices) {
        this.subDevices = subDevices;
    }

    public void add(ParamsSubDeviceLogout item) {
        subDevices.add(item);
    }

    @Override
    public boolean checkLegal() {
        if (Utils.isEmpty(subDevices)) {
            return false;
        }
        for (ParamsSubDeviceLogout item : subDevices) {
            if (!item.checkLegal()) {
                return false;
            }
        }
        return true;
    }
}
