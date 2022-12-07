package com.cuiot.openservices.sdk;

import com.cuiot.openservices.sdk.config.ConfigFactory;
import com.cuiot.openservices.sdk.config.IAdapterConfig;
import com.cuiot.openservices.sdk.config.IDeviceConfig;
import com.cuiot.openservices.sdk.entity.*;
import com.cuiot.openservices.sdk.entity.request.*;
import com.cuiot.openservices.sdk.entity.response.Response;
import com.cuiot.openservices.sdk.entity.shadow.DeviceShadow;
import com.cuiot.openservices.sdk.exception.AdapterException;
import com.cuiot.openservices.sdk.handler.DownlinkHandler;
import com.cuiot.openservices.sdk.mqtt.IMqttConnection;
import com.cuiot.openservices.sdk.mqtt.MqttClient;
import com.cuiot.openservices.sdk.mqtt.MqttSubscribe;
import com.cuiot.openservices.sdk.mqtt.handler.UplinkApi;
import com.cuiot.openservices.sdk.mqtt.promise.MqttSubscribePromise;
import com.cuiot.openservices.sdk.mqtt.promise.MqttUnsubscribePromise;
import com.cuiot.openservices.sdk.mqtt.result.MqttConnectResult;
import com.cuiot.openservices.sdk.util.AssertUtils;
import com.cuiot.openservices.sdk.util.TopicUtils;
import com.cuiot.openservices.sdk.util.Utils;
import com.typesafe.config.ConfigValue;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 泛协议服务
 *
 * @author yht
 */
public class CloudAdapter implements IMqttConnection {

    /**
     * 泛协议服务状态
     */
    private static final int DISCONNECTED = 0;
    private static final int CONNECTING = 1;
    private static final int CONNECTED = 2;
    private static final int DISCONNECTING = 3;
    private static final int RECONNECTING = 4;
    private final Object serviceLock = new Object();
    private Logger logger = LoggerFactory.getLogger(CloudAdapter.class);
    /**
     * 重连任务线程池
     */
    private ScheduledThreadPoolExecutor reconnectThreadPoolExecutor = new ScheduledThreadPoolExecutor(1,
            (r, executor) -> logger.error("Task " + r.toString() + " rejected from " + executor.toString()));
    private ScheduledFuture scheduledFuture;
    private Long reconnectDelay = null;
    /**
     * 上行
     */
    private UplinkApi uplinkApi;
    /**
     * 下行
     */
    private DownlinkHandler downlinkHandler;
    private MqttClient mqttClient;

    private Service service;
    /**
     * 泛协议服务状态，初始状态为已断开
     */
    private int serviceState = DISCONNECTED;

    public CloudAdapter() {
    }

    /**
     * 用户下行处理器
     *
     * @param downlinkHandler 用户下行处理器
     */
    public void setDownlinkHandler(DownlinkHandler downlinkHandler) {
        this.downlinkHandler = downlinkHandler;
    }

    //---------------------------泛协议服务API-----------------------------

    /**
     * 启动：泛协议服务上线，同步接口
     *
     * @throws AdapterException 异常
     */
    public void start() throws AdapterException {
        service = createService();
        AssertUtils.check(service);

        synchronized (serviceLock) {
            // 0.校验状态
            if (serviceState == CONNECTING || serviceState == RECONNECTING) {
                // 正在连接中
                throw new AdapterException(ReturnCode.SERVICE_IN_CONNECTING);
            } else if (serviceState == CONNECTED) {
                // 已连接
                throw new AdapterException(ReturnCode.SERVICE_IN_CONNECTED);
            } else if (serviceState == DISCONNECTING) {
                // 正在断开中
                throw new AdapterException(ReturnCode.SERVICE_IN_DISCONNECTING);
            }

            serviceState = CONNECTING;
            boolean ret = false;
            try {
                // 1.泛协议服务连接平台
                mqttClient = new MqttClient(downlinkHandler, this);
                MqttConnectResult result = mqttClient.connect(service.getClientId(), service.getUsername(), service.getPassword());
                if (result.returnCode() != MqttConnectReturnCode.CONNECTION_ACCEPTED) {
                    logger.error("service connect fail, {}", result);
                    throw new AdapterException(ReturnCode.SERVICE_CONNECT_ERROR);
                }
                // 3.连接成功，订阅成功
                uplinkApi = new UplinkApi(mqttClient);
                ret = true;
                logger.info("service connect success");
            } catch (Exception e) {
                logger.error("service connect fail, exception", e);
                throw new AdapterException(ReturnCode.SERVICE_CONNECT_ERROR);
            } finally {
                if (ret) {
                    serviceState = CONNECTED;
                } else {
                    serviceState = DISCONNECTED;
                    if (mqttClient != null) {
                        mqttClient.disconnectSilent();
                    }
                    mqttClient = null;
                    uplinkApi = null;
                }
            }
        }
    }

    private void innerReconnect() {
        Service service = createService();
        boolean ret = false;
        try {
            // 1.泛协议服务连接平台
            mqttClient = new MqttClient(downlinkHandler, this);
            MqttConnectResult result = mqttClient.connect(service.getClientId(), service.getUsername(), service.getPassword());
            if (result.returnCode() == MqttConnectReturnCode.CONNECTION_ACCEPTED) {
                uplinkApi = new UplinkApi(mqttClient);
                ret = true;
                logger.info("service reconnect success");
            } else {
                logger.error("service reconnect fail, {}", result);
            }
        } catch (Exception e) {
            logger.error("service reconnect fail, exception", e);
        }

        if (!ret) {
            // 失败，继续重连
            if (mqttClient != null) {
                mqttClient.disconnectSilent();
            }
            mqttClient = null;
            uplinkApi = null;
            innerStartReconnectTask();
        } else {
            // 成功，则停止重连，重置
            reconnectDelay = null;
            synchronized (serviceLock) {
                if (serviceState == RECONNECTING) {
                    serviceState = CONNECTED;
                    downlinkHandler.onServiceReconnected();
                }
            }
        }
    }

    private void innerStartReconnectTask() {
        synchronized (serviceLock) {
            if (serviceState == RECONNECTING) {
                scheduledFuture = reconnectThreadPoolExecutor.schedule(() -> innerReconnect(), getDelayTime(), TimeUnit.SECONDS);
            }
        }
    }

    private void startReconnectTask() {
        synchronized (serviceLock) {
            serviceState = RECONNECTING;
            scheduledFuture = reconnectThreadPoolExecutor.schedule(() -> innerReconnect(), getDelayTime(), TimeUnit.SECONDS);
        }
    }

    private long getDelayTime() {
        IAdapterConfig adapterConfig = ConfigFactory.getAdapterConfig();
        // 指数增长
        if (reconnectDelay == null) {
            reconnectDelay = adapterConfig.getReconnectInterval();
        } else {
            reconnectDelay <<= 1;
        }

        if (reconnectDelay > adapterConfig.getMaxReconnectInterval() || reconnectDelay <= 0) {
            reconnectDelay = adapterConfig.getReconnectInterval();
        }
        logger.info("getDelayTime reconnectDelay:{}", reconnectDelay);
        return reconnectDelay;
    }

    private Service createService() {
        IAdapterConfig adapterConfig = ConfigFactory.getAdapterConfig();
        return Service.builder()
                .serviceId(adapterConfig.getServiceId())
                .serviceKey(adapterConfig.getServiceKey())
                .serviceSecret(adapterConfig.getServiceSecret())
                .signMethod(adapterConfig.getSignMethod())
                .build();
    }

    private boolean subscribe(Device device) {
        try {
            List<MqttSubscribe> subList = new ArrayList<>();
            List<SubscribeTopic> topics = TopicUtils.createSubscribeList(service.getServiceId(), device);
            for (SubscribeTopic subscribeTopic : topics) {
                subList.add(new MqttSubscribe(subscribeTopic.getMqttQoS(), subscribeTopic.getTopic()));
            }
            MqttSubscribePromise promise = mqttClient.subscribe(subList);
            // TODO 优化: 默认超时
            promise.get(10, TimeUnit.SECONDS);
            return promise.isAllSuccess();
        } catch (Exception exception) {
            logger.error("exception occurs: ", exception);
            return false;
        }
    }

    private boolean unSubscribe(Device device) {
        try {
            logger.info("start to unsubcribe");
            List<String> topics = TopicUtils.createUnSubscribeList(service.getServiceId(), device);
            Future<MqttUnsubAckMessage> promise = mqttClient.unsubscribe(topics);
            // TODO 优化: 默认超时
            promise.get(10, TimeUnit.SECONDS);
            logger.info("unsubcribe finish, {}", promise.isSuccess());

            return promise.isSuccess();
        } catch (Exception exception) {
            logger.error("exception occurs: ", exception);
            return false;
        }

    }

    /**
     * 停止：泛协议服务下线，同步接口
     *
     * @throws AdapterException 异常
     */
    public void stop() throws AdapterException {
        synchronized (serviceLock) {
            // 0.校验状态
            if (serviceState == DISCONNECTED) {
                // 已断开
                throw new AdapterException(ReturnCode.SERVICE_IN_DISCONNECTED);
            } else if (serviceState == CONNECTING) {
                // 正在连接中
                throw new AdapterException(ReturnCode.SERVICE_IN_CONNECTING);
            } else if (serviceState == DISCONNECTING) {
                // 正在断开中
                throw new AdapterException(ReturnCode.SERVICE_IN_DISCONNECTING);
            }

            serviceState = DISCONNECTING;
            try {
                // 断开连接
                if (mqttClient != null) {
                    mqttClient.disconnect();
                }
                // 重置重连
                resetReconnectTask();
            } catch (Exception e) {
                logger.error("exception", e);
            } finally {
                // TODO 异常处理
                serviceState = DISCONNECTED;
                mqttClient = null;
                uplinkApi = null;
            }
        }
    }

    private void resetReconnectTask() {
        if (scheduledFuture != null) {
            boolean ret = scheduledFuture.cancel(true);
            logger.info("reset reconnect task ret:{}", ret);
        }
        reconnectDelay = null;
    }

    private void checkServiceState(int dstState, ReturnCode errorReturnCode) {
        synchronized (serviceLock) {
            if (serviceState != dstState) {
                throw new AdapterException(errorReturnCode);
            }
        }
    }

    //---------------------------设备API-----------------------------

    /**
     * 直连设备上线
     * 上线会订阅设备
     *
     * @param device 设备
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public CallableFuture<DeviceResult> deviceLogin(Device device) throws AdapterException {
        AssertUtils.check(device);
        // TODO 校验泛协议服务状态的同步块是否要包含发送业务功能
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        logger.info("start to login");
        // 1.订阅设备对应topic
        if (!subscribe(device)) {
            logger.error("service device {} connect fail, subscribe error", device.getOriginalIdentity());
            throw new AdapterException(ReturnCode.SERVICE_SUBSCRIBE_ERROR);
        }
        logger.info("login finish");

        return uplinkApi.doDeviceLogin(device, service);
    }

    /**
     * 直连设备下线
     *
     * @param device 设备
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public DeviceResult deviceLogout(Device device) throws AdapterException {
        AssertUtils.check(device);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);

        try {
            CallableFuture<DeviceResult> resultCallableFuture = uplinkApi.doDeviceLogout(device);
            DeviceResult deviceResult = resultCallableFuture.get(10, TimeUnit.SECONDS);
            // 1.取消订阅设备对应topic
            unSubscribe(device);
            return deviceResult;
        } catch (Exception e) {
            logger.error("device  {} logout failed", device.getOriginalIdentity());
            throw new AdapterException(ReturnCode.DEVICE_LOGOUT_FAILED);
        }
    }

    /**
     * 子设备上线
     *
     * @param gateway   网关
     * @param subDevice 子设备
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public CallableFuture<DeviceResult> subDeviceLogin(Device gateway, Device subDevice) throws AdapterException {
        AssertUtils.check(gateway, subDevice);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        if (!subscribe(subDevice)) {
            logger.error("service device {} connect fail, subscribe error", subDevice.getOriginalIdentity());
            throw new AdapterException(ReturnCode.SERVICE_SUBSCRIBE_ERROR);
        }

        return uplinkApi.doSubDeviceLogin(service.getUuid(), gateway, subDevice);
    }

    /**
     * 子设备下线
     *
     * @param gateway   网关
     * @param subDevice 子设备
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public DeviceResult subDeviceLogout(Device gateway, Device subDevice) throws AdapterException {
        AssertUtils.check(gateway, subDevice);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        try {
            CallableFuture<DeviceResult> deviceResultCallableFuture = uplinkApi.doSubDeviceLogout(gateway, subDevice);
            DeviceResult deviceResult = deviceResultCallableFuture.get(10, TimeUnit.SECONDS);
            // 1.取消订阅设备对应topic
            unSubscribe(subDevice);
            return deviceResult;
        } catch (Exception e) {
            logger.error("device  {} logout failed", subDevice.getOriginalIdentity());
            throw new AdapterException(ReturnCode.DEVICE_LOGOUT_FAILED);
        }
    }

    /**
     * 子设备批量上线
     *
     * @param gateway    网关
     * @param subDevices 子设备列表
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public CallableFuture<DeviceResult> subDeviceBatchLogin(Device gateway, List<Device> subDevices) throws
            AdapterException {
        AssertUtils.check(gateway);
        if (Utils.isEmpty(subDevices)) {
            return Utils.createResultCallback(ReturnCode.DATA_ILLEGAL);
        }
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        for (Device device : subDevices) {
            if (!subscribe(device)) {
                logger.error("service device {} connect fail, subscribe error", device.getOriginalIdentity());
                throw new AdapterException(ReturnCode.SERVICE_SUBSCRIBE_ERROR);
            }
        }
        return uplinkApi.doSubDeviceBatchLogin(service.getUuid(), gateway, subDevices);
    }

    /**
     * 子设备批量下线
     *
     * @param gateway    网关
     * @param subDevices 子设备列表
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public DeviceResult subDeviceBatchLogout(Device gateway, List<Device> subDevices) throws
            AdapterException {
        AssertUtils.check(gateway);
        if (Utils.isEmpty(subDevices)) {
            return Utils.createResult(ReturnCode.DATA_ILLEGAL);
        }
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);

        CallableFuture<DeviceResult> resultCallableFuture = uplinkApi.doSubDeviceBatchLogout(gateway, subDevices);
        try {
            DeviceResult deviceResult = resultCallableFuture.get(10, TimeUnit.SECONDS);
            for (Device device : subDevices) {
                if (!unSubscribe(device)) {
                    logger.error("service device {} connect fail, subscribe error", device.getOriginalIdentity());
                    throw new AdapterException(ReturnCode.SERVICE_SUBSCRIBE_ERROR);
                }
            }
            return deviceResult;
        } catch (Exception e) {
            logger.error("", e);
            throw new AdapterException(ReturnCode.DEVICE_LOGOUT_FAILED);
        }
    }

    /**
     * 直连设备属性上报
     *
     * @param device 设备
     * @param key    key
     * @param value  value
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public CallableFuture<DeviceResult> propertyPub(Device device, String key, Object value) throws
            AdapterException {
        AssertUtils.check(device);
        Request<ParamsPropertyPub> request = new Request<>();
        request.setParams(new ParamsPropertyPub(key, value));
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        return uplinkApi.doThingPublish(device, request, MessageType.PROPERTY_PUB);
    }

    /**
     * 直连设备属性上报
     *
     * @param device  设备
     * @param request 请求
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public CallableFuture<DeviceResult> propertyPub(Device device, Request<ParamsPropertyPub> request) throws
            AdapterException {
        AssertUtils.check(device, request);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        return uplinkApi.doThingPublish(device, request, MessageType.PROPERTY_PUB);
    }

    /**
     * 直连设备属性批量上报
     *
     * @param device              设备
     * @param paramsPropertyBatch 请求
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public CallableFuture<DeviceResult> propertyBatch(Device device, ParamsPropertyBatch paramsPropertyBatch) throws
            AdapterException {
        AssertUtils.check(device, paramsPropertyBatch);
        Request<ParamsPropertyBatch> request = new Request<>(paramsPropertyBatch);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        return uplinkApi.doThingPublish(device, request, MessageType.PROPERTY_BATCH);
    }

    /**
     * 直连设备属性批量上报
     *
     * @param device  设备
     * @param request 请求
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public CallableFuture<DeviceResult> propertyBatch(Device device, Request<ParamsPropertyBatch> request) throws
            AdapterException {
        AssertUtils.check(device, request);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        return uplinkApi.doThingPublish(device, request, MessageType.PROPERTY_BATCH);
    }

    /**
     * 直连设备事件上报
     *
     * @param device    设备
     * @param eventData 请求
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public CallableFuture<DeviceResult> eventPub(Device device, EventData eventData) throws AdapterException {
        AssertUtils.check(device);
        Request<EventData> request = new Request<>(eventData);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        return uplinkApi.doThingPublish(device, request, MessageType.EVENT_PUB);
    }

    /**
     * 直连设备事件上报
     *
     * @param device  设备
     * @param request 请求
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public CallableFuture<DeviceResult> eventPub(Device device, Request<ParamsEventPub> request) throws
            AdapterException {
        AssertUtils.check(device, request);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        return uplinkApi.doThingPublish(device, request, MessageType.EVENT_PUB);
    }


    /**
     * 直连设备事件批量上报
     *
     * @param device     设备
     * @param eventBatch 请求
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public CallableFuture<DeviceResult> eventBatch(Device device, EventBatch eventBatch) throws AdapterException {
        AssertUtils.check(device, eventBatch);
        Request<EventBatch> request = new Request<>(eventBatch);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        return uplinkApi.doThingPublish(device, request, MessageType.EVENT_BATCH);
    }

    /**
     * 直连设备事件批量上报
     *
     * @param device  设备
     * @param request 请求
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public CallableFuture<DeviceResult> eventBatch(Device device, Request<ParamsEventBatch> request) throws
            AdapterException {
        AssertUtils.check(device, request);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        return uplinkApi.doThingPublish(device, request, MessageType.EVENT_BATCH);
    }

    /**
     * 响应设置属性请求
     *
     * @param device   设备
     * @param response 响应
     * @return String
     * @throws AdapterException 异常
     */
    public String propertySetReply(Device device, Response response) throws AdapterException {
        AssertUtils.check(device, response);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        return uplinkApi.doReply(device, response, MessageType.PROPERTY_SET_REPLY);
    }

    /**
     * 响应服务下发请求
     *
     * @param device   设备
     * @param response 响应
     * @param sync     是否同步，即来源是平台同步下发服务，还是异步下发服务
     * @return String
     * @throws AdapterException 异常
     */
    public String servicePubReply(Device device, Response response, boolean sync) throws AdapterException {
        AssertUtils.check(device, response);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        return uplinkApi.doReply(device, response, sync ? MessageType.SYNC_PUB_REPLY : MessageType.SERVICE_PUB_REPLY);
    }

    /**
     * 直连设备获取设备影子
     *
     * @param device 设备
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public CallableFuture<DeviceResult> deviceShadowGet(Device device) throws AdapterException {
        AssertUtils.check(device);
        Request request = new Request();
        request.setParams(request.getMessageId());
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        return uplinkApi.doThingPublish(device, request, MessageType.DEVICE_SHADOW_GET);
    }

    /**
     * 响应平台下发直连设备影子命令
     *
     * @param device   设备
     * @param response 响应
     * @return String
     * @throws AdapterException 异常
     */
    public String deviceShadowCommandReply(Device device, Response response) throws AdapterException {
        AssertUtils.check(device, response);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        return uplinkApi.doReply(device, response, MessageType.DEVICE_SHADOW_COMMAND_REPLY);
    }

    /**
     * 直连设备更新设备影子
     *
     * @param device  设备
     * @param request 请求
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public CallableFuture<DeviceResult> deviceShadowUpdate(Device device, Request<DeviceShadow> request) throws
            AdapterException {
        AssertUtils.check(device, request);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        return uplinkApi.doThingPublish(device, request, MessageType.DEVICE_SHADOW_UPDATE);
    }

    /**
     * 子设备属性事件批量上报
     *
     * @param gateway 网关设备
     * @param request 请求
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public CallableFuture<DeviceResult> propertyEventPub(Device gateway, Request<SubDevicePropertyEventPub> request)
            throws AdapterException {
        AssertUtils.check(gateway, request);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        request.getParams().setUuid(service.getUuid());
        return uplinkApi.doThingPublish(gateway, request, MessageType.PROPERTY_EVENT_PUB);
    }

    /**
     * 子设备绑定拓扑关系
     *
     * @param gateway 网关设备
     * @param request 请求
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public CallableFuture<DeviceResult> subDeviceBindTopo(Device gateway, Request<SubDeviceTopoUpdate.SubDeviceTopoUpdateParams> request)
            throws AdapterException {
        AssertUtils.check(gateway, request);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        request.getParams().setUuid(service.getUuid());
        return uplinkApi.doThingPublish(gateway, request, MessageType.SUB_DEVICE_ADD_TOPO);
    }

    /**
     * 子设备解绑拓扑关系
     *
     * @param gateway 网关设备
     * @param request 请求
     * @return CallableFuture
     * @throws AdapterException 异常
     */
    public CallableFuture<DeviceResult> subDeviceDeleteTopo(Device gateway,  Request<SubDeviceTopoUpdate.SubDeviceTopoUpdateParams> request)
            throws AdapterException {
        AssertUtils.check(gateway, request);
        checkServiceState(CONNECTED, ReturnCode.SERVICE_NOT_CONNECTED);
        request.getParams().setUuid(service.getUuid());
        return uplinkApi.doThingPublish(gateway, request, MessageType.SUB_DEVICE_DELETE_TOPO);
    }

    @Override
    public void onConnected() {
    }

    @Override
    public void onDisconnected() {
        // TODO 访问权限
        logger.info("onDisconnected");
        IAdapterConfig adapterConfig = ConfigFactory.getAdapterConfig();
        synchronized (serviceLock) {
            if (serviceState == CONNECTED) {
                if (adapterConfig.enableReconnect()) {
                    startReconnectTask();
                }
                // 当前是连接状态，断开，则通知第三方应用
                downlinkHandler.onServiceDisconnected();
            }
        }
    }
}
