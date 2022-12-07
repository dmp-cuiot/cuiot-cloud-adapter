package com.cuiot.openservices.sdk.entity.request;

import com.cuiot.openservices.sdk.entity.ICheck;
import com.cuiot.openservices.sdk.util.Utils;
import io.netty.util.internal.StringUtil;

import java.util.List;

/**
 * @author zhh
 */
public class ParamsSubDevicePropertyEventPub implements ICheck {

    /**
     * 子设备产品key
     */
    private String productKey;

    /**
     * 子设备设备key
     */
    private String deviceKey;

    /*
    * 子设备唯一标识符
    * */
    private String originalIdentity;
    /**
     * 属性集
     */
    private List<ParamsPropertyPub> properties;

    /**
     * 事件集
     */
    private List<ParamsEventPub> events;

    public List<ParamsPropertyPub> getProperties() {
        return properties;
    }

    public void setProperties(List<ParamsPropertyPub> properties) {
        this.properties = properties;
    }

    public List<ParamsEventPub> getEvents() {
        return events;
    }

    public void setEvents(List<ParamsEventPub> events) {
        this.events = events;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public String getOriginalIdentity() {
        return originalIdentity;
    }

    public void setOriginalIdentity(String originalIdentity) {
        this.originalIdentity = originalIdentity;
    }

    @Override
    public boolean checkLegal() {
        if (Utils.isEmpty(properties) && Utils.isEmpty(events)) {
            return false;
        }
        if ( StringUtil.isNullOrEmpty(originalIdentity)) {
            return false;
        }
        return true;
    }
}
