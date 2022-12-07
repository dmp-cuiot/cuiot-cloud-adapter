package com.cuiot.openservices.sdk.entity.request;

import com.cuiot.openservices.sdk.entity.ICheck;
import com.cuiot.openservices.sdk.util.CheckArrayList;
import com.cuiot.openservices.sdk.util.Utils;

/**
 * 请求参数params：属性批量上报
 *
 * @author yht
 */
public class ParamsEventBatch implements ICheck {
    private CheckArrayList<ParamsEventPub> data = new CheckArrayList<>();

    public ParamsEventBatch() {
    }

    public CheckArrayList<ParamsEventPub> getData() {
        return data;
    }

    public void setData(CheckArrayList<ParamsEventPub> data) {
        this.data = data;
    }

    public void add(ParamsEventPub item) {
        data.add(item);
    }

    @Override
    public boolean checkLegal() {
        if (Utils.isEmpty(data)) {
            return false;
        }
        for (ParamsEventPub item : data) {
            if (!item.checkLegal()) {
                return false;
            }
        }
        return true;
    }
}
