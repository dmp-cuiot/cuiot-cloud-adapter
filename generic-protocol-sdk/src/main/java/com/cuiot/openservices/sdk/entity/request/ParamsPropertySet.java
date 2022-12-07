package com.cuiot.openservices.sdk.entity.request;

import com.cuiot.openservices.sdk.entity.ICheck;
import io.netty.util.internal.StringUtil;

/**
 * 请求参数params：设置属性
 *
 * @author yht
 */
public class ParamsPropertySet implements ICheck {
    private String key;
    private Object value;
    private String ts;

    public ParamsPropertySet() {
    }

    public ParamsPropertySet(String key, Object value) {
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

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    @Override
    public boolean checkLegal() {
        return !StringUtil.isNullOrEmpty(key) && value != null;
    }
}
