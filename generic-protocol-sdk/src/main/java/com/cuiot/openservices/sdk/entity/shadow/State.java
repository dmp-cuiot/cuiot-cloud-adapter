package com.cuiot.openservices.sdk.entity.shadow;

import com.alibaba.fastjson.JSONObject;

/**
 * 设备影子 state
 *
 * @author yht
 */
public class State {

    /**
     * 设备影子的属性值
     */
    private JSONObject reported;

    /**
     * 设备影子的期望属性值
     */
    private JSONObject desired;

    public JSONObject getReported() {
        return reported;
    }

    public void setReported(JSONObject reported) {
        this.reported = reported;
    }

    public JSONObject getDesired() {
        return desired;
    }

    public void setDesired(JSONObject desired) {
        this.desired = desired;
    }
}
