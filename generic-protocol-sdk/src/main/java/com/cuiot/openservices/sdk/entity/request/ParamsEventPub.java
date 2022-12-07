package com.cuiot.openservices.sdk.entity.request;

import com.cuiot.openservices.sdk.entity.ICheck;
import com.cuiot.openservices.sdk.util.Utils;
import io.netty.util.internal.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 请求参数params：事件上报
 *
 * @author yht
 */
public class ParamsEventPub implements ICheck {
    private String key;
    private String ts = String.valueOf(System.currentTimeMillis());
    private List<EventItem> info = new ArrayList<>();

    public ParamsEventPub() {
    }

    public ParamsEventPub(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public List<EventItem> getInfo() {
        return info;
    }

    public void setInfo(List<EventItem> info) {
        this.info = info;
    }

    public void addEventItem(String key, Object value) {
        EventItem eventItem = new EventItem(key, value);
        info.add(eventItem);
    }

    @Override
    public boolean checkLegal() {
        if (StringUtil.isNullOrEmpty(key) || Utils.isEmpty(info)) {
            return false;
        }
        for (EventItem eventItem : info) {
            if (!eventItem.checkLegal()) {
                return false;
            }
        }
        return true;
    }

    public static class EventItem implements ICheck {
        private String key;
        private Object value;

        public EventItem() {
        }

        public EventItem(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        @Override
        public boolean checkLegal() {
            return !StringUtil.isNullOrEmpty(key) && value != null;
        }
    }

}
