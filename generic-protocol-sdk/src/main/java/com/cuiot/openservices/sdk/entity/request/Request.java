package com.cuiot.openservices.sdk.entity.request;

import com.alibaba.fastjson.JSON;
import com.cuiot.openservices.sdk.constant.CommonConstant;
import com.cuiot.openservices.sdk.entity.ICheck;
import com.cuiot.openservices.sdk.util.MessageIdUtils;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * 请求类：包含SDK请求平台、平台请求SDK
 *
 * @author yht
 */
public class Request<T> implements ICheck {

    private static Logger logger = LoggerFactory.getLogger(Request.class);

    private String messageId = MessageIdUtils.generateId();

    private T params;

    public Request() {
    }

    public Request(T params) {
        this.params = params;
    }

    public static boolean checkLegal(Request request) {
        return request != null && request.checkLegal();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public T getParams() {
        return params;
    }

    public void setParams(T params) {
        this.params = params;
    }

    public byte[] encode() {
        logger.info("request encode:{}", toJsonString());
        return toJsonString().getBytes(Charset.forName(CommonConstant.MQTT_CODEC_DMP));
    }

    public String toJsonString() {
        return JSON.toJSONString(this);
    }

    @Override
    public String toString() {
        return "Request{" +
                "messageId='" + messageId + '\'' +
                ", params=" + params +
                '}';
    }

    @Override
    public boolean checkLegal() {
        //移除parms为null判断 params == null
        if (StringUtil.isNullOrEmpty(messageId)) {
            return false;
        }
        if (params instanceof ICheck) {
            ICheck iCheck = (ICheck) params;
            if (!iCheck.checkLegal()) {
                return false;
            }
        }
        return true;
    }
}
