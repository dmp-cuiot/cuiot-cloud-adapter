package com.cuiot.openservices.sdk.util;

import com.cuiot.openservices.sdk.constant.CommonConstant;
import com.cuiot.openservices.sdk.entity.Device;
import com.cuiot.openservices.sdk.entity.request.*;
import com.cuiot.openservices.sdk.exception.InvalidMqttTopicException;
import com.cuiot.openservices.sdk.exception.UnsupportedMqttMessageTypeException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 消息组装
 *
 * @author unicom
 */
public final class ProtocolMessageUtils {

    private static Logger logger = LoggerFactory.getLogger(ProtocolMessageUtils.class);

    public static ParamsDeviceLogin createParamsDeviceLogin(Device device, String uuid) {
        ParamsDeviceLogin params = new ParamsDeviceLogin();
        params.setOriginalIdentity(device.getOriginalIdentity());
        params.setAuthType(device.getAuthType());
        params.setOperator(CommonConstant.OPERATOR_ZERO);
        params.setDeviceId(device.getDeviceId());
        params.setSignMethod(device.getSignMethod());
        params.setSign(device.getPassword());
        params.setUuid(uuid);
        return params;
    }

    public static MqttMessage createDeviceLoginMsg(String serviceId, Device device, byte[] data) {
        String topic = TopicUtils.createDeviceLoginTopic(serviceId, device.getProductKey(), device.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    /**
     * 直连设备下线消息
     *
     * @param serviceId 泛协议服务ID
     * @param device    设备
     * @return MqttMessage
     */
    public static MqttMessage createDeviceLogoutMsg(String serviceId, Device device) {
        String topic = TopicUtils.createDeviceLogoutTopic(serviceId, device.getProductKey(), device.getDeviceKey());
        return createPublishMessage(topic, null);
    }

    public static ParamsSubDeviceLogin createParamsSubDeviceLogin(String uuid, Device subDevice) {
        ParamsSubDeviceLogin params = new ParamsSubDeviceLogin();
        params.setOriginalIdentity(subDevice.getOriginalIdentity());
        params.setAuthType(subDevice.getAuthType());
        params.setProductKey(subDevice.getProductKey());
        params.setDeviceKey(subDevice.getDeviceKey());
        params.setDeviceId(subDevice.getDeviceId());
        params.setSignMethod(subDevice.getSignMethod());
        params.setSign(subDevice.getPassword());
        params.setUuid(uuid);
        return params;
    }

    public static ParamsSubDeviceLogout createParamsSubDeviceLogout(Device subDevice) {
        ParamsSubDeviceLogout params = new ParamsSubDeviceLogout();
        params.setProductKey(subDevice.getProductKey());
        params.setDeviceKey(subDevice.getDeviceKey());
        return params;
    }

    public static MqttMessage createSubDeviceLoginMsg(String serviceId, Device gateway, byte[] data) {
        String topic = String.format(TopicUtils.SUB_DEVICE_LOGIN_TOPIC_FORMAT, serviceId, gateway.getProductKey(), gateway.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    public static MqttMessage createSubDeviceLogoutMsg(String serviceId, Device gateway, byte[] data) {
        String topic = String.format(TopicUtils.SUB_DEVICE_LOGOUT_TOPIC_FORMAT, serviceId, gateway.getProductKey(), gateway.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    public static ParamsSubDeviceBatchLogin createParamsSubDeviceBatchLogin(String uuid, List<Device> subDevices) {
        ParamsSubDeviceBatchLogin params = new ParamsSubDeviceBatchLogin();
        for (Device subDevice : subDevices) {
            params.add(createParamsSubDeviceLogin(uuid, subDevice));
        }
        return params;
    }

    public static MqttMessage createSubDeviceBatchLoginMsg(String serviceId, Device gateway, byte[] data) {
        String topic = String.format(TopicUtils.SUB_DEVICE_BATCH_LOGIN_TOPIC_FORMAT, serviceId, gateway.getProductKey(), gateway.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    public static ParamsSubDeviceBatchLogout createParamsSubDeviceBatchLogout(List<Device> subDevices) {
        ParamsSubDeviceBatchLogout params = new ParamsSubDeviceBatchLogout();
        for (Device subDevice : subDevices) {
            params.add(createParamsSubDeviceLogout(subDevice));
        }
        return params;
    }

    public static MqttMessage createSubDeviceBatchLogoutMsg(String serviceId, Device gateway, byte[] data) {
        String topic = String.format(TopicUtils.SUB_DEVICE_BATCH_LOGOUT_TOPIC_FORMAT, serviceId, gateway.getProductKey(), gateway.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    /**
     * 创建MQTT设备属性上传消息
     *
     * @param serviceId 泛协议服务ID
     * @param device    设备
     * @param data      数据
     * @return MqttMessage
     */
    public static MqttMessage createPropertyPubMsg(String serviceId, Device device, byte[] data) {
        String topic = TopicUtils.createPropertyPubTopic(serviceId, device.getProductKey(), device.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    public static MqttMessage createPropertyBatchMsg(String serviceId, Device device, byte[] data) {
        String topic = String.format(TopicUtils.PROPERTY_BATCH_TOPIC_FORMAT, serviceId, device.getProductKey(), device.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    public static MqttMessage createEventPubMsg(String serviceId, Device device, byte[] data) {
        String topic = TopicUtils.createEventPubTopic(serviceId, device.getProductKey(), device.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    public static MqttMessage createEventBatchMsg(String serviceId, Device device, byte[] data) {
        String topic = String.format(TopicUtils.EVENT_BATCH_TOPIC_FORMAT, serviceId, device.getProductKey(), device.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    /**
     * 创建回复设置属性请求
     *
     * @param serviceId 泛协议服务ID
     * @param device    设备
     * @param data      数据
     * @return MqttMessage
     */
    public static MqttMessage createPropertySetReplyMsg(String serviceId, Device device, byte[] data) {
        String topic = TopicUtils.createPropertySetReplyTopic(serviceId, device.getProductKey(), device.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    public static MqttMessage createSyncPubReplyMsg(String serviceId, Device device, byte[] data) {
        String topic = String.format(TopicUtils.SYNC_PUB_REPLY_TOPIC_FORMAT, serviceId, device.getProductKey(), device.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    public static MqttMessage createServicePubReplyMsg(String serviceId, Device device, byte[] data) {
        String topic = String.format(TopicUtils.SERVICE_PUB_REPLY_TOPIC_FORMAT, serviceId, device.getProductKey(), device.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    public static MqttMessage createDeviceShadowGetMsg(String serviceId, Device device, byte[] data) {
        String topic = String.format(TopicUtils.DEVICE_SHADOW_GET_TOPIC_FORMAT, serviceId, device.getProductKey(), device.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    public static MqttMessage createDeviceShadowCommandReplyMsg(String serviceId, Device device, byte[] data) {
        String topic = String.format(TopicUtils.DEVICE_SHADOW_COMMAND_REPLY_TOPIC_FORMAT, serviceId, device.getProductKey(), device.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    public static MqttMessage createDeviceShadowUpdateMsg(String serviceId, Device device, byte[] data) {
        String topic = String.format(TopicUtils.DEVICE_SHADOW_UPDATE_TOPIC_FORMAT, serviceId, device.getProductKey(), device.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    public static MqttMessage createSubDeviceAddTopoMsg(String serviceId, Device device, byte[] data) {
        String topic = String.format(TopicUtils.SUB_TOPO_ADD_TOPIC_FORMAT, serviceId, device.getProductKey(), device.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    public static MqttMessage createSubDeviceDeleteTopoMsg(String serviceId, Device device, byte[] data) {
        String topic = String.format(TopicUtils.SUB_TOPO_DELETE_TOPIC_FORMAT, serviceId, device.getProductKey(), device.getDeviceKey());
        return createPublishMessage(topic, data);
    }

    public static MqttPublishMessage validateMqttMessage(MqttMessage mqttMessage) {
        if (MqttMessageType.PUBLISH != mqttMessage.fixedHeader().messageType()) {
            throw new UnsupportedMqttMessageTypeException("only publish message could be decoded to DeviceMessage");
        }
        MqttPublishMessage publishMessage = (MqttPublishMessage) mqttMessage;
        String[] array = TopicUtils.splitTopic(publishMessage.variableHeader().topicName());
        if (array.length < CommonConstant.TOPIC_SPLIT_MIN_LENGTH) {
            throw new InvalidMqttTopicException("downlink topic level less than 5");
        }
        return publishMessage;
    }

    /**
     * 上行消息
     *
     * @param topic   mqtt topic
     * @param payload payload
     * @return MqttMessage
     */
    private static MqttMessage createPublishMessage(String topic, byte[] payload) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(topic, 0);
        payload = payload == null ? new byte[0] : payload;
        ByteBuf byteBuf = Unpooled.wrappedBuffer(payload);
        return new MqttPublishMessage(fixedHeader, variableHeader, byteBuf);
    }

    /**
     * 子设备属性事件批量上报
     *
     * @param serviceId 泛协议服务ID
     * @param device    设备
     * @param data      数据
     * @return MqttMessage
     */
    public static MqttMessage createSubDevicePropertyEventBatch(String serviceId, Device device, byte[] data) {
        String topic = TopicUtils.createSubDevicePropertyEventBatch(serviceId, device.getProductKey(), device.getDeviceKey());

        return createPublishMessage(topic, data);
    }
}
