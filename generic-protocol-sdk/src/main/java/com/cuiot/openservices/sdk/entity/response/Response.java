package com.cuiot.openservices.sdk.entity.response;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cuiot.openservices.sdk.entity.ICheck;
import com.cuiot.openservices.sdk.entity.request.Request;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * 响应类：包含SDK响应平台、平台响应SDK
 *
 * @author yht
 */
public class Response<T> implements ICheck {
    private static Logger logger = LoggerFactory.getLogger(Request.class);

    private String code;
    private String message;
    private String messageId;
    private T data;

    public Response() {
    }

    public Response(String messageId, String code, String message) {
        this.messageId = messageId;
        this.code = code;
        this.message = message;
    }

    public Response(String messageId, String code, String message, T data) {
        this(messageId, code, message);
        this.data = data;
    }

    public static Response decode(String payload) {
        try {
            JSONObject jsonObject = JSON.parseObject(payload);
            if (jsonObject == null) {
                return null;
            }

            logger.info("response decode:{}", jsonObject.toJSONString());
            return JSON.parseObject(payload, Response.class);
        } catch (Exception e) {
            logger.error("decode exception", e);
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Response{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", messageId='" + messageId + '\'' +
                ", data=" + data +
                '}';
    }

    public byte[] encode() {
        logger.info("response encode:{}", JSON.toJSONString(this));
        return JSON.toJSONString(this).getBytes(Charset.defaultCharset());
    }


    @Override
    public boolean checkLegal() {
        return !StringUtil.isNullOrEmpty(code) && !StringUtil.isNullOrEmpty(messageId);
    }

}
