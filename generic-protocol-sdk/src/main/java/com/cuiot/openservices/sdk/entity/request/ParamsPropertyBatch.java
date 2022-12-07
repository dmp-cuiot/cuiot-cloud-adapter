package com.cuiot.openservices.sdk.entity.request;

import com.cuiot.openservices.sdk.entity.ICheck;
import com.cuiot.openservices.sdk.util.CheckArrayList;
import com.cuiot.openservices.sdk.util.Utils;

/**
 * 请求参数params：属性批量上报
 *
 * @author yht
 */
public class ParamsPropertyBatch implements ICheck {
    private CheckArrayList<ParamsPropertyPub> data = new CheckArrayList<>();

    public ParamsPropertyBatch() {
    }

    public CheckArrayList<ParamsPropertyPub> getData() {
        return data;
    }

    public void setData(CheckArrayList<ParamsPropertyPub> data) {
        this.data = data;
    }

    public void add(ParamsPropertyPub item) {
        data.add(item);
    }

    @Override
    public boolean checkLegal() {
        if (Utils.isEmpty(data)) {
            return false;
        }
        for (ParamsPropertyPub item : data) {
            if (!item.checkLegal()) {
                return false;
            }
        }
        return true;
    }
}
