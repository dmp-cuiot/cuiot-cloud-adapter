package com.cuiot.openservices.sdk.entity.request;

import com.cuiot.openservices.sdk.entity.ICheck;
import com.cuiot.openservices.sdk.util.CheckArrayList;
import com.cuiot.openservices.sdk.util.Utils;

/**
 * 请求参数params：子设备批量上线
 *
 * @author yht
 */
public class ParamsSubDeviceBatchLogin implements ICheck {
    private CheckArrayList<ParamsSubDeviceLogin> subDevices = new CheckArrayList<>();

    public ParamsSubDeviceBatchLogin() {
    }

    public CheckArrayList<ParamsSubDeviceLogin> getSubDevices() {
        return subDevices;
    }

    public void setSubDevices(CheckArrayList<ParamsSubDeviceLogin> subDevices) {
        this.subDevices = subDevices;
    }

    public void add(ParamsSubDeviceLogin item) {
        subDevices.add(item);
    }

    @Override
    public boolean checkLegal() {
        if (Utils.isEmpty(subDevices)) {
            return false;
        }
        for (ParamsSubDeviceLogin item : subDevices) {
            if (!item.checkLegal()) {
                return false;
            }
        }
        return true;
    }
}
