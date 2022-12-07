package com.cuiot.openservices.sdk.entity.request;

import com.cuiot.openservices.sdk.entity.ICheck;
import com.cuiot.openservices.sdk.util.CheckArrayList;
import com.cuiot.openservices.sdk.util.Utils;

/**
 * @author wanghy
 */
public class EventBatch implements ICheck {

    private CheckArrayList<EventData> info = new CheckArrayList<>();

    public EventBatch() {

    }

    public void add(EventData eventData){
        this.info.add(eventData);
    }
    public CheckArrayList<EventData> getInfo() {
        return info;
    }

    public void setInfo(CheckArrayList<EventData> info) {
        this.info = info;
    }

    @Override
    public boolean checkLegal() {
        if (Utils.isEmpty(info)) {
            return false;
        }
        for (EventData item : info) {
            if (!item.checkLegal()) {
                return false;
            }
        }
        return true;
    }
}
