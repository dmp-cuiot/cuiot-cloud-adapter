package com.cuiot.openservices.sdk.mqtt;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.IllegalReferenceCountException;

import static java.util.Objects.requireNonNull;

/**
 * @author unicom
 */
public class MqttArticle implements ByteBufHolder {

    private MqttQoS qos;
    private boolean retain = false;
    private String topic;
    private ByteBuf payload;
    private boolean isWill = false;

    public MqttArticle() {
    }

    public MqttArticle(MqttQoS qos, boolean retain, String topic, ByteBuf payload) {
        this.qos = qos;
        this.retain = retain;
        this.topic = topic;
        this.payload = payload;
    }

    public MqttArticle(MqttQoS qos, boolean retain, String topic, ByteBuf payload, boolean isWill) {
        this.qos = requireNonNull(qos, "qos");
        this.retain = retain;
        this.topic = topic;
        this.payload = requireNonNull(payload, "payload");
        this.isWill = isWill;
    }

    public MqttQoS qos() {
        return qos;
    }

    public boolean isRetain() {
        return retain;
    }

    public String topic() {
        return topic;
    }

    public ByteBuf payload() {
        return content();
    }

    public byte[] payloadAsBytes() {
        if (payload == null) {
            return null;
        }
        ByteBuf payload = content().duplicate();
        byte[] bytes = new byte[payload.capacity()];
        payload.readBytes(bytes);
        return bytes;
    }

    @Override
    public int refCnt() {
        return payload.refCnt();
    }

    @Override
    public boolean release() {
        if (payload == null) {
            return true;
        }
        return payload.release();
    }

    @Override
    public boolean release(int decrement) {
        return payload.release(decrement);
    }

    @Override
    public ByteBuf content() {
        final int refCnt = refCnt();
        if (refCnt > 0) {
            return payload;
        }
        throw new IllegalReferenceCountException(refCnt);
    }

    @Override
    public MqttArticle copy() {
        return replace(content().copy());
    }

    @Override
    public MqttArticle duplicate() {
        return replace(content().duplicate());
    }

    @Override
    public MqttArticle retainedDuplicate() {
        return replace(content().retainedDuplicate());
    }

    @Override
    public MqttArticle replace(ByteBuf content) {
        return new MqttArticle(qos, retain, topic, content, isWill);
    }

    @Override
    public MqttArticle retain() {
        content().retain();
        return this;
    }

    @Override
    public MqttArticle retain(int increment) {
        content().retain(increment);
        return this;
    }

    @Override
    public MqttArticle touch() {
        content().touch();
        return this;
    }

    @Override
    public MqttArticle touch(Object hint) {
        content().touch(hint);
        return this;
    }

    public MqttQoS getQos() {
        return qos;
    }

    public void setQos(MqttQoS qos) {
        this.qos = qos;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public ByteBuf getPayload() {
        return payload;
    }

    public void setPayload(ByteBuf payload) {
        this.payload = payload;
    }

    public boolean isWill() {
        return isWill;
    }

    public void setWill(boolean will) {
        isWill = will;
    }
}
