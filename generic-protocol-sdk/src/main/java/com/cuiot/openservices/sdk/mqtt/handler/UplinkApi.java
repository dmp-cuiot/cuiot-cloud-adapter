package com.cuiot.openservices.sdk.mqtt.handler;

import com.cuiot.openservices.sdk.config.ConfigFactory;
import com.cuiot.openservices.sdk.config.IAdapterConfig;
import com.cuiot.openservices.sdk.entity.*;
import com.cuiot.openservices.sdk.entity.request.*;
import com.cuiot.openservices.sdk.entity.response.Response;
import com.cuiot.openservices.sdk.mqtt.MqttClient;
import com.cuiot.openservices.sdk.mqtt.promise.DevicePromise;
import com.cuiot.openservices.sdk.mqtt.promise.PromiseCanceller;
import com.cuiot.openservices.sdk.util.ProtocolMessageUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 用于上行消息给平台
 *
 * @author yht
 */
public final class UplinkApi {

    private Logger logger = LoggerFactory.getLogger(UplinkApi.class);

    private IAdapterConfig adapterConfig;
    private MqttClient mqttClient;

    public UplinkApi(MqttClient mqttClient) {
        this.adapterConfig = ConfigFactory.getAdapterConfig();
        this.mqttClient = mqttClient;
    }

    /**
     * 直连设备上线
     *
     * @param device 设备
     * @return CallableFuture
     */
    public CallableFuture<DeviceResult> doDeviceLogin(Device device, Service service) {
        String productKey = device.getProductKey();
        String deviceKey = device.getDeviceKey();
        ParamsDeviceLogin params = ProtocolMessageUtils.createParamsDeviceLogin(device, service.getUuid());
        Request<ParamsDeviceLogin> request = new Request<>(params);
        MqttMessage mqttMessage = ProtocolMessageUtils.createDeviceLoginMsg(adapterConfig.getServiceId(), device, request.encode());
        Channel channel = mqttClient.getChannel();
        DevicePromise<DeviceResult> promise = new DevicePromise<>(mqttMessage, productKey, deviceKey, MessageType.DEVICE_LOGIN, channel.eventLoop());
        promise.setId(request.getMessageId());
        // TODO Canceller
        channel.writeAndFlush(promise).addListener(new PromiseCanceller<>(promise));
        return promise;
    }

    /**
     * 直连设备下线
     *
     * @param device 设备
     * @return CallableFuture
     */
    public CallableFuture<DeviceResult> doDeviceLogout(Device device) {
        String productKey = device.getProductKey();
        String deviceKey = device.getDeviceKey();
        MqttMessage mqttMessage = ProtocolMessageUtils.createDeviceLogoutMsg(adapterConfig.getServiceId(), device);
        Channel channel = mqttClient.getChannel();
        DevicePromise<DeviceResult> promise = new DevicePromise<>(mqttMessage, productKey, deviceKey, MessageType.DEVICE_LOGOUT, channel.eventLoop());
        channel.writeAndFlush(promise).addListener(new PromiseCanceller<>(promise));
        return promise;
    }

    public CallableFuture<DeviceResult> doSubDeviceLogin(String uuid, Device gateway, Device subDevice) {
        String productKey = subDevice.getProductKey();
        String deviceKey = subDevice.getDeviceKey();
        ParamsSubDeviceLogin params = ProtocolMessageUtils.createParamsSubDeviceLogin(uuid, subDevice);
        Request<ParamsSubDeviceLogin> request = new Request<>(params);
        MqttMessage mqttMessage = ProtocolMessageUtils.createSubDeviceLoginMsg(adapterConfig.getServiceId(), gateway, request.encode());
        Channel channel = mqttClient.getChannel();
        DevicePromise<DeviceResult> promise = new DevicePromise<>(mqttMessage, productKey, deviceKey, MessageType.SUB_DEVICE_LOGIN, channel.eventLoop());
        promise.setId(request.getMessageId());
        channel.writeAndFlush(promise).addListener(new PromiseCanceller<>(promise));
        return promise;
    }

    public CallableFuture<DeviceResult> doSubDeviceLogout(Device gateway, Device subDevice) {
        String productKey = subDevice.getProductKey();
        String deviceKey = subDevice.getDeviceKey();
        ParamsSubDeviceLogout params = ProtocolMessageUtils.createParamsSubDeviceLogout(subDevice);
        Request<ParamsSubDeviceLogout> request = new Request<>(params);
        MqttMessage mqttMessage = ProtocolMessageUtils.createSubDeviceLogoutMsg(adapterConfig.getServiceId(), gateway, request.encode());
        Channel channel = mqttClient.getChannel();
        DevicePromise<DeviceResult> promise = new DevicePromise<>(mqttMessage, productKey, deviceKey, MessageType.SUB_DEVICE_LOGOUT, channel.eventLoop());
        channel.writeAndFlush(promise).addListener(new PromiseCanceller<>(promise));
        return promise;
    }

    public CallableFuture<DeviceResult> doSubDeviceBatchLogin(String uuid, Device gateway, List<Device> subDevices) {
        ParamsSubDeviceBatchLogin params = ProtocolMessageUtils.createParamsSubDeviceBatchLogin(uuid, subDevices);
        Request<ParamsSubDeviceBatchLogin> request = new Request<>(params);
        MqttMessage mqttMessage = ProtocolMessageUtils.createSubDeviceBatchLoginMsg(adapterConfig.getServiceId(), gateway, request.encode());
        Channel channel = mqttClient.getChannel();
        DevicePromise<DeviceResult> promise = new DevicePromise<>(mqttMessage, MessageType.SUB_DEVICE_BATCH_LOGIN, channel.eventLoop());
        promise.setId(request.getMessageId());
        channel.writeAndFlush(promise).addListener(new PromiseCanceller<>(promise));
        return promise;
    }

    public CallableFuture<DeviceResult> doSubDeviceBatchLogout(Device gateway, List<Device> subDevices) {
        ParamsSubDeviceBatchLogout params = ProtocolMessageUtils.createParamsSubDeviceBatchLogout(subDevices);
        Request<ParamsSubDeviceBatchLogout> request = new Request<>(params);
        MqttMessage mqttMessage = ProtocolMessageUtils.createSubDeviceBatchLogoutMsg(adapterConfig.getServiceId(), gateway, request.encode());
        Channel channel = mqttClient.getChannel();
        DevicePromise<DeviceResult> promise = new DevicePromise<>(mqttMessage, MessageType.SUB_DEVICE_BATCH_LOGOUT, channel.eventLoop());
        promise.setId(request.getMessageId());
        channel.writeAndFlush(promise).addListener(new PromiseCanceller<>(promise));
        return promise;
    }

    /**
     * 响应数据
     *
     * @param device      设备
     * @param response    响应
     * @param messageType 响应数据类型
     * @return String
     */
    public String doReply(Device device, Response response, MessageType messageType) {
        doReplyThingRequest(device, response, messageType);
        return response.getMessageId();
    }

    /**
     * 发布数据到平台
     *
     * @param device      设备
     * @param request     请求
     * @param messageType 上传类型
     * @return CallableFuture
     */
    public CallableFuture<DeviceResult> doThingPublish(Device device, Request request, MessageType messageType) {
        String productKey = device.getProductKey();
        String deviceKey = device.getDeviceKey();
        MqttMessage mqttMessage;
        DevicePromise<DeviceResult> promise = new DevicePromise<>(productKey, deviceKey);
        promise.setMessageType(messageType);
        byte[] data = request.encode();
        switch (messageType) {
            case PROPERTY_PUB:
                mqttMessage = ProtocolMessageUtils.createPropertyPubMsg(adapterConfig.getServiceId(), device, data);
                break;
            case PROPERTY_BATCH:
                mqttMessage = ProtocolMessageUtils.createPropertyBatchMsg(adapterConfig.getServiceId(), device, data);
                break;
            case EVENT_PUB:
                mqttMessage = ProtocolMessageUtils.createEventPubMsg(adapterConfig.getServiceId(), device, data);
                break;
            case EVENT_BATCH:
                mqttMessage = ProtocolMessageUtils.createEventBatchMsg(adapterConfig.getServiceId(), device, data);
                break;
            case PROPERTY_EVENT_PUB:
                mqttMessage = ProtocolMessageUtils.createSubDevicePropertyEventBatch(adapterConfig.getServiceId(), device, data);
                break;
            case DEVICE_SHADOW_GET:
                mqttMessage = ProtocolMessageUtils.createDeviceShadowGetMsg(adapterConfig.getServiceId(), device, data);
                break;
            case DEVICE_SHADOW_COMMAND_REPLY:
                mqttMessage = ProtocolMessageUtils.createDeviceShadowCommandReplyMsg(adapterConfig.getServiceId(), device, data);
                break;
            case DEVICE_SHADOW_UPDATE:
                mqttMessage = ProtocolMessageUtils.createDeviceShadowUpdateMsg(adapterConfig.getServiceId(), device, data);
                break;
            case SUB_DEVICE_ADD_TOPO:
                mqttMessage = ProtocolMessageUtils.createSubDeviceAddTopoMsg(adapterConfig.getServiceId(), device, data);
                break;
            case SUB_DEVICE_DELETE_TOPO:
                mqttMessage = ProtocolMessageUtils.createSubDeviceDeleteTopoMsg(adapterConfig.getServiceId(), device, data);
                break;
            default:
                logger.warn("unknown upload type:{}", messageType);
                promise = new DevicePromise<>();
                promise.trySuccess(new DeviceResult(ReturnCode.SDK_ERROR));
                return promise;
        }
        promise.setMqttMessage(mqttMessage);
        promise.setId(request.getMessageId());
        Channel channel = mqttClient.getChannel();
        channel.writeAndFlush(promise).addListener(new PromiseCanceller<>(promise));
        return promise;
    }

    /**
     * 回复平台请求
     *
     * @param device      设备
     * @param response    响应
     * @param messageType 响应类型
     */
    private void doReplyThingRequest(Device device, Response response, MessageType messageType) {
        byte[] data = response.encode();
        MqttMessage mqttMessage;
        switch (messageType) {
            case PROPERTY_SET_REPLY:
                mqttMessage = ProtocolMessageUtils.createPropertySetReplyMsg(adapterConfig.getServiceId(), device, data);
                break;
            case SYNC_PUB_REPLY:
                mqttMessage = ProtocolMessageUtils.createSyncPubReplyMsg(adapterConfig.getServiceId(), device, data);
                break;
            case SERVICE_PUB_REPLY:
                mqttMessage = ProtocolMessageUtils.createServicePubReplyMsg(adapterConfig.getServiceId(), device, data);
                break;
            case DEVICE_SHADOW_COMMAND_REPLY:
                mqttMessage = ProtocolMessageUtils.createDeviceShadowCommandReplyMsg(adapterConfig.getServiceId(), device, data);
                break;
            default:
                logger.warn("unknown thing request reply type:{}", messageType);
                return;
        }
        mqttClient.getChannel().writeAndFlush(mqttMessage);
    }

}
