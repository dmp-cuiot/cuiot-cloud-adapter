package com.cuiot.openservices.sdk.mqtt.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.cuiot.openservices.sdk.constant.CommonConstant;
import com.cuiot.openservices.sdk.entity.Device;
import com.cuiot.openservices.sdk.entity.DeviceResult;
import com.cuiot.openservices.sdk.entity.ReturnCode;
import com.cuiot.openservices.sdk.entity.request.ParamsPropertySet;
import com.cuiot.openservices.sdk.entity.request.ParamsServicePub;
import com.cuiot.openservices.sdk.entity.request.Request;
import com.cuiot.openservices.sdk.entity.response.DeviceItem;
import com.cuiot.openservices.sdk.entity.response.Response;
import com.cuiot.openservices.sdk.entity.shadow.DeviceShadow;
import com.cuiot.openservices.sdk.exception.AdapterException;
import com.cuiot.openservices.sdk.handler.DownlinkHandler;
import com.cuiot.openservices.sdk.mqtt.DeviceSessionManager;
import com.cuiot.openservices.sdk.mqtt.IMqttConnection;
import com.cuiot.openservices.sdk.mqtt.MqttFixedHeaders;
import com.cuiot.openservices.sdk.mqtt.promise.DevicePromise;
import com.cuiot.openservices.sdk.mqtt.promise.PromiseBreaker;
import com.cuiot.openservices.sdk.util.CheckArrayList;
import com.cuiot.openservices.sdk.util.ProtocolMessageUtils;
import com.cuiot.openservices.sdk.util.TopicUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.netty.channel.ChannelFutureListener.CLOSE_ON_FAILURE;

/**
 * 消息处理
 *
 * @author yht
 */
public final class ProtocolMessageHandler extends ChannelDuplexHandler {

    private final Logger logger = LoggerFactory.getLogger(ProtocolMessageHandler.class);
    private final DownlinkHandler downlinkHandler;
    private final IMqttConnection iMqttConnection;
    /**
     * 设备上线Promise映射：productKey+deviceKey -> promise 用于登录消息限制
     */
    private Cache<String, AtomicReference<DevicePromise<DeviceResult>>> cacheDeviceLoginPromise;
    /**
     * 设备下线Promise映射：productKey+deviceKey -> promise 用于登出消息限制
     */
    private Cache<String, AtomicReference<DevicePromise<DeviceResult>>> cacheDeviceLogoutPromise;
    /**
     * 设备上行Promise映射 messageId -> promise
     */
    private Cache<String, DevicePromise<DeviceResult>> cacheDeviceUploadPromise;

    public ProtocolMessageHandler(DownlinkHandler downlinkHandler, IMqttConnection iMqttConnection) {
        this.downlinkHandler = downlinkHandler;
        this.iMqttConnection = iMqttConnection;
        cacheDeviceLoginPromise = CacheBuilder.newBuilder().expireAfterWrite(CommonConstant.CACHE_MESSAGE_TIME, TimeUnit.SECONDS).build();
        cacheDeviceLogoutPromise = CacheBuilder.newBuilder().expireAfterWrite(CommonConstant.CACHE_MESSAGE_TIME, TimeUnit.SECONDS).build();
        cacheDeviceUploadPromise = CacheBuilder.newBuilder().expireAfterWrite(CommonConstant.CACHE_MESSAGE_TIME, TimeUnit.SECONDS).build();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("exceptionCaught cause:", cause);
        ctx.close().addListener(CLOSE_ON_FAILURE);
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        MqttMessage mqttMessage = (MqttMessage) msg;
        Integer packetId = null;
        try {
            MqttPublishMessage publishMessage = ProtocolMessageUtils.validateMqttMessage(mqttMessage);
            packetId = publishMessage.variableHeader().packetId();
            dispatchPublishMessage(publishMessage);
        } catch (Exception e) {
            logger.warn("catch exception ", e);
        } finally {
            try {
                if (MqttQoS.AT_LEAST_ONCE == mqttMessage.fixedHeader().qosLevel() && packetId != null) {
                    MqttPubAckMessage mqttPubAckMessage = new MqttPubAckMessage(MqttFixedHeaders.PUBACK_HEADER, MqttMessageIdVariableHeader.from(packetId));
                    channelHandlerContext.writeAndFlush(mqttPubAckMessage);
                }
            } catch (Exception e) {
                logger.error("puback error", e);
            }
            ReferenceCountUtil.release(mqttMessage);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise channelPromise) throws Exception {
        if (msg instanceof DevicePromise<?>) {
            MqttMessage mqttMessage;
            DevicePromise<DeviceResult> promise = (DevicePromise<DeviceResult>) msg;
            mqttMessage = promise.getMqttMessage();
            switch (((DevicePromise<?>) msg).getMessageType()) {
                // 单个设备上下线 上行
                case DEVICE_LOGIN:
                case SUB_DEVICE_LOGIN:
                    handleDeviceLogin(ctx, channelPromise, mqttMessage, promise);
                    break;
                case DEVICE_LOGOUT:
                case SUB_DEVICE_LOGOUT:
                    handleDeviceLogout(ctx, channelPromise, mqttMessage, promise);
                    break;
                // 子设备批量上下线 / 其他上下行业务
                case SUB_DEVICE_BATCH_LOGIN:
                case SUB_DEVICE_BATCH_LOGOUT:
                case PROPERTY_PUB:
                case PROPERTY_BATCH:
                case EVENT_PUB:
                case EVENT_BATCH:
                case PROPERTY_EVENT_PUB:
                case DEVICE_SHADOW_GET:
                case DEVICE_SHADOW_COMMAND_REPLY:
                case DEVICE_SHADOW_UPDATE:
                case SUB_DEVICE_ADD_TOPO:
                case SUB_DEVICE_DELETE_TOPO:
                    cacheDeviceUploadPromise.put(promise.getId(), promise);
                    super.write(ctx, mqttMessage, channelPromise);
                    break;
                default:
                    super.write(ctx, mqttMessage, channelPromise);
                    break;
            }
        } else {
            super.write(ctx, msg, channelPromise);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        PromiseBreaker promiseBreaker = new PromiseBreaker(new ClosedChannelException());
        for (Map.Entry<String, AtomicReference<DevicePromise<DeviceResult>>> entry : cacheDeviceLoginPromise.asMap().entrySet()) {
            promiseBreaker.renege(entry.getValue().getAndSet(null));
        }
        for (Map.Entry<String, AtomicReference<DevicePromise<DeviceResult>>> entry : cacheDeviceLogoutPromise.asMap().entrySet()) {
            promiseBreaker.renege(entry.getValue().getAndSet(null));
        }
        promiseBreaker.renege(cacheDeviceUploadPromise.asMap().values());
        iMqttConnection.onDisconnected();
    }

    /**
     * 平台下行消息分发
     *
     * @param publishMessage 平台下行消息
     */
    private void dispatchPublishMessage(MqttPublishMessage publishMessage) {
        String topic = publishMessage.variableHeader().topicName();
        String[] array = TopicUtils.splitTopic(topic);
        String productKey = array[2];
        String deviceKey = array[3];
        logger.info("dispatchPublishMessage topic:{}", topic);

        String topicPostfix = TopicUtils.getPostfixFromTopic(topic);
        byte[] data = new byte[publishMessage.payload().readableBytes()];
        publishMessage.payload().readBytes(data);
        String payload = new String(data, Charset.forName(CommonConstant.MQTT_CODEC_DMP));
        switch (topicPostfix) {
            // 直连设备、网关上下线 下行
            case TopicUtils.DEVICE_LOGIN_REPLY:
                handleDeviceLoginReply(productKey, deviceKey, payload);
                break;
            case TopicUtils.DEVICE_LOGOUT_REPLY:
                handleDeviceLogoutReply(productKey, deviceKey, payload);
                break;
            // 子设备上下线 下行
            case TopicUtils.SUB_DEVICE_LOGIN_REPLY:
                handleSubDeviceLoginReply(productKey, deviceKey, payload);
                break;
            case TopicUtils.SUB_DEVICE_LOGOUT_REPLY:
                handleSubDeviceLogoutReply(productKey, deviceKey, payload);
                break;
            // 子设备批量上下线 下行
            case TopicUtils.SUB_DEVICE_BATCH_LOGIN_REPLY:
                handleSubDeviceBatchLoginReply(productKey, deviceKey, payload);
                break;
            case TopicUtils.SUB_DEVICE_BATCH_LOGOUT_REPLY:
                handleSubDeviceBatchLogoutReply(productKey, deviceKey, payload);
                break;
            // 设置属性、服务调用 下行
            case TopicUtils.PROPERTY_SET:
                handlePropertySet(productKey, deviceKey, payload);
                break;
            case TopicUtils.SYNC_PUB:
                handleServicePub(productKey, deviceKey, payload, true);
                break;
            case TopicUtils.SERVICE_PUB:
                handleServicePub(productKey, deviceKey, payload, false);
                break;
            // 设备影子：get reply
            case TopicUtils.DEVICE_SHADOW_GET_REPLY:
                handleDeviceShadowGetReply(productKey, deviceKey, payload);
                break;
            // 设备影子：command
            case TopicUtils.DEVICE_SHADOW_COMMAND:
                handleDeviceShadowCommand(productKey, deviceKey, payload);
                break;
            // reply业务 下行：属性上报reply，事件上报reply，设备影子更新reply
            case TopicUtils.PROPERTY_PUB_REPLY:
            case TopicUtils.SUB_DEVICE_TOPO_ADD_REPLY:
            case TopicUtils.SUB_DEVICE_TOPO_DELETE_REPLY:
            case TopicUtils.EVENT_PUB_REPLY:
            case TopicUtils.DEVICE_SHADOW_UPDATE_REPLY:
            default:
                Response response = Response.decode(payload);
                logger.info("default topic:{} response:{}", topic, response);
                if (response != null) {
                    DevicePromise<DeviceResult> promise = cacheDeviceUploadPromise.getIfPresent(response.getMessageId());
                    if (promise != null) {
                        promise.trySuccess(new DeviceResult(response));
                        cacheDeviceUploadPromise.invalidate(response.getMessageId());
                    } else {
                        logger.warn("current cache promise is {}", cacheDeviceUploadPromise.asMap());
                        logger.warn("received unexpected data, data:{}", response);
                    }
                }
        }
    }

    private void handleDeviceLogin(ChannelHandlerContext ctx, ChannelPromise channelPromise,
                                   MqttMessage mqttMessage, DevicePromise<DeviceResult> promise) throws Exception {
        String productKey = promise.getProductKey();
        String deviceKey = promise.getDeviceKey();
        // TODO DevicePromise外层AtomicReference
        AtomicReference<DevicePromise<DeviceResult>> reference = new AtomicReference<>();
        reference.set(promise);
        // TODO 优化 线程同步
        AtomicReference<DevicePromise<DeviceResult>> previous = cacheDeviceLoginPromise.getIfPresent(productKey + "-" + deviceKey);
        if (previous != null) {
            promise.trySuccess(new DeviceResult(ReturnCode.REPEATED_REQUEST));
            ReferenceCountUtil.release(mqttMessage);
        } else {
            // 成功则缓存promise
            cacheDeviceLoginPromise.put(productKey + "-" + deviceKey, reference);
            super.write(ctx, mqttMessage, channelPromise);
        }
    }

    private void handleDeviceLogout(ChannelHandlerContext ctx, ChannelPromise channelPromise,
                                    MqttMessage mqttMessage, DevicePromise<DeviceResult> promise) throws Exception {
        String productKey = promise.getProductKey();
        String deviceKey = promise.getDeviceKey();
        AtomicReference<DevicePromise<DeviceResult>> reference = new AtomicReference<>();
        reference.set(promise);
        AtomicReference<DevicePromise<DeviceResult>> previous = cacheDeviceLogoutPromise.getIfPresent(productKey + "-" + deviceKey);
        if (previous != null) {
            promise.trySuccess(new DeviceResult(ReturnCode.REPEATED_REQUEST));
            ReferenceCountUtil.release(mqttMessage);
        } else {
            // 成功则缓存promise
            cacheDeviceLogoutPromise.put(productKey + "-" + deviceKey, reference);
            super.write(ctx, mqttMessage, channelPromise);
        }
    }

    private void handleDeviceLoginReply(String productKey, String deviceKey, String payload) {
        Response response = Response.decode(payload);
        if (response == null) {
            return;
        }

        if (ReturnCode.SUCCESS.getCode().equals(response.getCode())) {
            DeviceSessionManager.putDeviceSession(productKey, deviceKey);
            resetCacheDeviceLoginPromise(new DeviceItem(productKey, deviceKey), true, response);
        } else {
            resetCacheDeviceLoginPromise(new DeviceItem(productKey, deviceKey), false, response);
        }
    }

    private void handleDeviceLogoutReply(String productKey, String deviceKey, String payload) {
        Response response = Response.decode(payload);
        if (response == null) {
            return;
        }

        // TODO logout 失败响应处理
        DeviceSessionManager.handleDeviceLogout(productKey, deviceKey);
        resetCacheDeviceLogoutPromise(new DeviceItem(productKey, deviceKey), true, response);
    }

    private void handleSubDeviceLoginReply(String gatewayProductKey, String gatewayDeviceKey, String payload) {
        Response<ArrayList<DeviceItem>> response = JSON.parseObject(payload, new TypeReference<Response<ArrayList<DeviceItem>>>() {
        });
        if (response == null) {
            return;
        }

        // TODO 失败时，data是否为空
        DeviceItem subDevice = response.getData() != null ? response.getData().get(0) : null;
        if (ReturnCode.SUCCESS.getCode().equals(response.getCode())) {
            // 成功则创建session
            DeviceSessionManager.putDeviceSession(subDevice);
            resetCacheDeviceLoginPromise(subDevice, true, response);
        } else {
            resetCacheDeviceLoginPromise(subDevice, false, response);
        }
    }

    private void handleSubDeviceBatchLoginReply(String gatewayProductKey, String gatewayDeviceKey, String payload) {
        Response<ArrayList<DeviceItem>> response = JSON.parseObject(payload, new TypeReference<Response<ArrayList<DeviceItem>>>() {
        });
        if (response == null) {
            return;
        }

        if (ReturnCode.SUCCESS.getCode().equals(response.getCode())) {
            // 成功则创建session
            for (DeviceItem subDevice : response.getData()) {
                DeviceSessionManager.putDeviceSession(subDevice);
            }
            resetCacheDeviceUploadPromise(true, response);
        } else {
            resetCacheDeviceUploadPromise(false, response);
        }
    }

    private void handleSubDeviceLogoutReply(String gatewayProductKey, String gatewayDeviceKey, String payload) {
        Response<ArrayList<DeviceItem>> response = JSON.parseObject(payload, new TypeReference<Response<ArrayList<DeviceItem>>>() {
        });
        if (response == null) {
            return;
        }

        DeviceItem subDevice = response.getData() != null ? response.getData().get(0) : null;
        if (ReturnCode.SUCCESS.getCode().equals(response.getCode())) {
            // 成功则删除会话
            DeviceSessionManager.handleDeviceLogout(subDevice);
            resetCacheDeviceLogoutPromise(subDevice, true, response);
        } else {
            resetCacheDeviceLogoutPromise(subDevice, false, response);
        }
    }

    private void handleSubDeviceBatchLogoutReply(String gatewayProductKey, String gatewayDeviceKey, String payload) {
        Response<ArrayList<DeviceItem>> response = JSON.parseObject(payload, new TypeReference<Response<ArrayList<DeviceItem>>>() {
        });
        if (response == null) {
            return;
        }

        if (ReturnCode.SUCCESS.getCode().equals(response.getCode())) {
            // 成功则删除会话
            for (DeviceItem subDevice : response.getData()) {
                DeviceSessionManager.handleDeviceLogout(subDevice);
            }
            resetCacheDeviceUploadPromise(true, response);
        } else {
            resetCacheDeviceUploadPromise(false, response);
        }
    }

    private void handlePropertySet(String productKey, String deviceKey, String payload) {
        logger.info("handlePropertySet productKey:{} deviceKey:{} payload:{}", productKey, deviceKey, payload);
        Request<CheckArrayList<ParamsPropertySet>> request = JSON.parseObject(payload, new TypeReference<Request<CheckArrayList<ParamsPropertySet>>>() {
        });
        // 直连设备、网关和子设备的设置属性topic一样，只是payload中多了productKey和deviceKey
        JSONObject jsonPayload = JSON.parseObject(payload);
        if (jsonPayload.containsKey(CommonConstant.PRODUCT_KEY) && jsonPayload.containsKey(CommonConstant.DEVICE_KEY)) {
            // 子设备：平台设置属性
            Device gateway = Device.builder().productKey(productKey).deviceKey(deviceKey).build();
            String subProductKey = jsonPayload.getString(CommonConstant.PRODUCT_KEY);
            String subDeviceKey = jsonPayload.getString(CommonConstant.DEVICE_KEY);
            String subOriginalIdentity = jsonPayload.getString(CommonConstant.ORIGINAL_IDENTITY);
            Device subDevice = Device.builder().productKey(subProductKey).deviceKey(subDeviceKey).originalIdentity(subOriginalIdentity).build();
            downlinkHandler.onPropertySetRequest(gateway, subDevice, request);
        } else {
            // 直连设备、网关：平台设置属性
            String originalIdentity = jsonPayload.getString(CommonConstant.ORIGINAL_IDENTITY);
            Device device = Device.builder().productKey(productKey).deviceKey(deviceKey).originalIdentity(originalIdentity).build();
            downlinkHandler.onPropertySetRequest(device, request);
        }
    }

    private void handleServicePub(String productKey, String deviceKey, String payload, boolean sync) {
        // 服务调用：同步，异步
        logger.info("handleServicePub productKey:{} deviceKey:{} payload:{}", productKey, deviceKey, payload);
        Request<ParamsServicePub> request = JSON.parseObject(payload, new TypeReference<Request<ParamsServicePub>>() {
        });
        // 直连设备、网关和子设备的服务调用topic一样，只是payload中多了productKey和deviceKey
        JSONObject jsonPayload = JSON.parseObject(payload);
        if (jsonPayload.containsKey(CommonConstant.PRODUCT_KEY) && jsonPayload.containsKey(CommonConstant.DEVICE_KEY)) {
            // 子设备：平台设置属性
            Device gateway = Device.builder().productKey(productKey).deviceKey(deviceKey).build();
            String subProductKey = jsonPayload.getString(CommonConstant.PRODUCT_KEY);
            String subDeviceKey = jsonPayload.getString(CommonConstant.DEVICE_KEY);
            String subOriginalIdentity = jsonPayload.getString(CommonConstant.ORIGINAL_IDENTITY);
            Device subDevice = Device.builder().productKey(subProductKey).deviceKey(subDeviceKey).originalIdentity(subOriginalIdentity).build();
            downlinkHandler.onServicePubRequest(gateway, subDevice, request, sync);
        } else {
            String originalIdentity = jsonPayload.getString(CommonConstant.ORIGINAL_IDENTITY);
            Device device = Device.builder().productKey(productKey).deviceKey(deviceKey).originalIdentity(originalIdentity).build();
            downlinkHandler.onServicePubRequest(device, request, sync);
        }
    }

    private void handleDeviceShadowGetReply(String productKey, String deviceKey, String payload) {
        // 设备影子：获取reply
        logger.info("handleDeviceShadowGetReply productKey:{} deviceKey:{} payload:{}", productKey, deviceKey, payload);
        Response<DeviceShadow> response = JSON.parseObject(payload, new TypeReference<Response<DeviceShadow>>() {
        });
        if (response != null) {
            DevicePromise<DeviceResult> promise = cacheDeviceUploadPromise.getIfPresent(response.getMessageId());
            if (promise != null) {
                promise.trySuccess(new DeviceResult(response));
                cacheDeviceUploadPromise.invalidate(response.getMessageId());
            } else {
                logger.warn("received unexpected data, data:{}", response);
            }
        }
    }

    private void handleDeviceShadowCommand(String productKey, String deviceKey, String payload) {
        // 设备影子：command
        logger.info("handleDeviceShadowCommand productKey:{} deviceKey:{} payload:{}", productKey, deviceKey, payload);
        Request<DeviceShadow> request = JSON.parseObject(payload, new TypeReference<Request<DeviceShadow>>() {
        });
        JSONObject jsonPayload = JSON.parseObject(payload);
        String originalIdentity = jsonPayload.getString(CommonConstant.ORIGINAL_IDENTITY);
        Device device = Device.builder().productKey(productKey).deviceKey(deviceKey).originalIdentity(originalIdentity).build();
        downlinkHandler.onDeviceShadowCommandRequest(device, request);
    }

    //--------------优化---------------

    private void resetCacheDeviceLoginPromise(DeviceItem deviceItem, boolean success, Response response) {
        if (deviceItem == null) {
            return;
        }

        String productKey = deviceItem.getProductKey();
        String deviceKey = deviceItem.getDeviceKey();
        String key = productKey + "-" + deviceKey;
        AtomicReference<DevicePromise<DeviceResult>> reference = cacheDeviceLoginPromise.getIfPresent(key);
        if (reference == null) {
            logger.warn("device login failed: reference is null");
            return;
        }

        DevicePromise<DeviceResult> promise = reference.get();
        if (success) {
            promise.trySuccess(new DeviceResult(response));
        } else {
            promise.tryFailure(new AdapterException(response.getCode(), response.getMessage()));
        }
        cacheDeviceLoginPromise.invalidate(key);
    }

    private void resetCacheDeviceLogoutPromise(DeviceItem deviceItem, boolean success, Response response) {
        if (deviceItem == null) {
            return;
        }

        String productKey = deviceItem.getProductKey();
        String deviceKey = deviceItem.getDeviceKey();
        String key = productKey + "-" + deviceKey;
        AtomicReference<DevicePromise<DeviceResult>> reference = cacheDeviceLogoutPromise.getIfPresent(key);
        if (reference == null) {
            logger.warn("device login failed: reference is null");
            return;
        }

        DevicePromise<DeviceResult> promise = reference.get();
        if (success) {
            promise.trySuccess(new DeviceResult(response));
        } else {
            promise.tryFailure(new AdapterException(response.getCode(), response.getMessage()));
        }
        cacheDeviceLogoutPromise.invalidate(key);
    }

    private void resetCacheDeviceUploadPromise(boolean success, Response response) {
        DevicePromise<DeviceResult> promise = cacheDeviceUploadPromise.getIfPresent(response.getMessageId());
        if (promise == null) {
            return;
        }

        if (success) {
            promise.trySuccess(new DeviceResult(response));
        } else {
            promise.tryFailure(new AdapterException(response.getCode(), response.getMessage()));
        }
        cacheDeviceUploadPromise.invalidate(response.getMessageId());
    }

}
