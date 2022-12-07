package com.cuiot.openservices.sdk.mqtt.promise;

import com.cuiot.openservices.sdk.entity.CallableFuture;
import com.cuiot.openservices.sdk.entity.MessageType;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;

/**
 * @author unicom
 */
public class DevicePromise<V> extends DefaultPromise<V> implements CallableFuture<V> {

    /**
     * TODO 本地消息id = 业务messageId
     */
    private String id;
    /**
     * TODO 优化
     */
    private String productKey;
    private String deviceKey;
    private MqttMessage mqttMessage;
    private MessageType messageType;

    public DevicePromise() {
    }

    public DevicePromise(String productKey, String deviceKey) {
        this.productKey = productKey;
        this.deviceKey = deviceKey;
    }

    public DevicePromise(MqttMessage mqttMessage, MessageType messageType, EventExecutor executor) {
        // TODO
        super(executor);
        this.mqttMessage = mqttMessage;
        this.messageType = messageType;
    }

    public DevicePromise(MqttMessage mqttMessage, String productKey, String deviceKey, MessageType messageType, EventExecutor executor) {
        super(executor);
        this.mqttMessage = mqttMessage;
        this.productKey = productKey;
        this.deviceKey = deviceKey;
        this.messageType = messageType;
    }

    @Override
    protected EventExecutor executor() {
        // TODO 优化
        EventExecutor e = super.executor();
        if (e == null) {
            return new DefaultEventExecutor();
        } else {
            return e;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductKey() {
        return productKey;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public MqttMessage getMqttMessage() {
        return mqttMessage;
    }

    public void setMqttMessage(MqttMessage mqttMessage) {
        this.mqttMessage = mqttMessage;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
