package com.cuiot.openservices.sdk.entity.request;

import com.cuiot.openservices.sdk.entity.ICheck;
import io.netty.util.internal.StringUtil;

import java.util.List;

/**
 * 请求参数params：服务下发
 *
 * @author yht
 */
public class ParamsServicePub implements ICheck {
    private String key;
    private String ts;
    private List<Info> info;

    public ParamsServicePub() {
    }

    public ParamsServicePub(String key) {
        this.key = key;
    }

    public ParamsServicePub(String key, String ts, List<Info> info) {
        this.key = key;
        this.ts = ts;
        this.info = info;
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

    public List<Info> getInfo() {
        return info;
    }

    public void setInfo(List<Info> info) {
        this.info = info;
    }

    @Override
    public boolean checkLegal() {
        return !StringUtil.isNullOrEmpty(key) && info != null;
    }

    public static class Info {
        private String key;
        private Object value;

        public Info() {
        }

        public Info(String key, Object value) {
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
    }
}
