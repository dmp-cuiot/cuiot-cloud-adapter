package com.cuiot.openservices.sdk.entity.request;

import com.cuiot.openservices.sdk.entity.ICheck;
import com.cuiot.openservices.sdk.util.Utils;
import io.netty.util.internal.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wanghy
 */
public class EventData implements ICheck {

    private String key;

    private String ts = String.valueOf(System.currentTimeMillis());

    private List<Event> info = new ArrayList<>();

    public EventData() {

    }

    public EventData(String key){
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<Event> getInfo() {
        return info;
    }

    public void setInfo(List<Event> info) {
        this.info = info;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public void addEvent(String key,Object value){
        Event event = new Event(key,value);
        this.info.add(event);
    }

    @Override
    public boolean checkLegal() {
        if (StringUtil.isNullOrEmpty(key) || Utils.isEmpty(info)) {
            return false;
        }
        for (Event item : info) {
            if (!item.checkLegal()) {
                return false;
            }
        }
        return true;
    }

}
