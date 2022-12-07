package com.cuiot.openservices.demo.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cuiot.openservices.demo.enums.ResultCode;
import com.cuiot.openservices.demo.handler.AutoDownLinkHandler;
import com.cuiot.openservices.sdk.CloudAdapter;
import com.cuiot.openservices.sdk.config.ConfigFactory;
import com.cuiot.openservices.sdk.config.IAdapterConfig;
import com.cuiot.openservices.sdk.config.IDeviceConfig;
import com.cuiot.openservices.sdk.constant.JsonResultUtil;
import com.cuiot.openservices.sdk.entity.CallableFuture;
import com.cuiot.openservices.sdk.entity.Device;
import com.cuiot.openservices.sdk.entity.DeviceResult;
import com.cuiot.openservices.sdk.entity.request.*;
import com.cuiot.openservices.sdk.entity.response.Response;
import com.cuiot.openservices.sdk.entity.shadow.DeviceShadow;
import com.cuiot.openservices.sdk.entity.shadow.State;
import com.cuiot.openservices.sdk.exception.AdapterException;
import com.cuiot.openservices.sdk.util.CheckArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * @author zhh
 * @date 2021/8/12 18:38
 * @description 接口实现层
 */
@Service
public class AdapterService {

    @Autowired
    public CloudAdapter cloudAdapter;
    private Logger logger = LoggerFactory.getLogger(AdapterService.class);
    @Autowired
    private IAdapterConfig adapterConfig;
    @Autowired
    private IDeviceConfig deviceConfig;

    @Autowired(required = false)
    private AutoDownLinkHandler autoDownLinkHandler;

    @PostConstruct
    public void init() {
        ConfigFactory.init(adapterConfig, deviceConfig);
        cloudAdapter.setDownlinkHandler(autoDownLinkHandler);
        cloudAdapter.start();
    }

    /**
     * 上传直连设备数据(物模型属性)
     *
     * @param device     设备信息
     * @param jsonObject 上传的数据信息
     * @return 返回信息
     */
    public Response uploadDevicePub(Device device, JSONObject jsonObject) {
        Integer type = jsonObject.getInteger(JsonResultUtil.TYPE);
        try {
            switch (type) {
                case 1:
                    //设备上线
                    return this.deviceLogin(device);
                case 2:
                    //设备下线
                    return this.deviceLogout(device);
                case 3:
                    //设备上报属性
                    return this.propertyPub(device, jsonObject);
                case 4:
                    //设备上报事件
                    return this.eventPub(device, jsonObject);
                case 5:
                    //设备批量上报属性
                    return this.propertyBatchPub(device, jsonObject);
                case 6:
                    //设备批量上报事件
                    return this.eventBatchPub(device, jsonObject);
                default:
                    return new Response(null, ResultCode.TYPE_EXISTENT.getCode(),
                            ResultCode.TOKEN_INVALID.getMessage());
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Response(null, ResultCode.DATA_ILLEGAL.getCode(), ResultCode.DATA_ILLEGAL.getMessage());
    }

    /**
     * 上传网关设备信息
     *
     * @param gateway    网关设备信息
     * @param subDevice  网关子设备信息
     * @param jsonObject 上报的数据信息
     * @return 返回信息
     */
    public Response uploadSubDevicePub(Device gateway, Device subDevice, JSONObject jsonObject) {
        Integer type = jsonObject.getInteger(JsonResultUtil.TYPE);
        try {
            switch (type) {
                case 1:
                    //网关子设备上线
                    return this.subDeviceLogin(gateway, subDevice);
                case 2:
                    //网关子设备下线
                    return this.subDeviceLogout(gateway, subDevice);
                default:
                    return new Response(null, ResultCode.TYPE_EXISTENT.getCode(),
                            ResultCode.TOKEN_INVALID.getMessage());
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Response(null, ResultCode.DATA_ILLEGAL.getCode(), ResultCode.DATA_ILLEGAL.getMessage());
    }

    /**
     * 网关子设备批量操作
     *
     * @param gateway    网关设备信息
     * @param subDevices 网关子设备集信息
     * @param jsonObject 上报数据信息
     * @return 返回信息
     */
    public Response uploadSubDeviceBatch(Device gateway, List<Device> subDevices, JSONObject jsonObject) {
        Integer type = jsonObject.getInteger(JsonResultUtil.TYPE);
        try {
            switch (type) {
                case 1:
                    //网关子设备批量上线
                    return this.subDeviceLoginBatch(gateway, subDevices);
                case 2:
                    //网关子设备批量下线
                    return this.subDeviceLogoutBatch(gateway, subDevices);
                case 3:
                    //网关子设备批量上报属性和事件
                    return this.subDevicePropertyEventPub(gateway, subDevices, jsonObject);
                case 4:
                    return this.subDeviceAddTopo(gateway, subDevices.get(0));
                case 5:
                    return this.subDeviceDeleteTopo(gateway, subDevices.get(0));
                default:
                    return new Response(null, ResultCode.TYPE_EXISTENT.getCode(),
                            ResultCode.TYPE_EXISTENT.getMessage());
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Response(null, ResultCode.DATA_ILLEGAL.getCode(), ResultCode.DATA_ILLEGAL.getMessage());
    }

    /**
     * 设备批量上报事件
     *
     * @param device     设备信息
     * @param jsonObject 上报数据信息
     * @return 返回信息
     */
    private Response eventBatchPub(Device device, JSONObject jsonObject)
            throws ExecutionException, InterruptedException {
        JSONArray data = jsonObject.getJSONArray(JsonResultUtil.DATA);
        // 封装参数
        CheckArrayList<EventData> info = new CheckArrayList<>();
        for (Object entity : data) {
            String jsonStr = entity.toString();
            EventData eventData = JSONObject.parseObject(jsonStr, EventData.class);
            info.add(eventData);
        }
        EventBatch eventBatch = new EventBatch();
        eventBatch.setInfo(info);
        //设备批量上报事件
        return cloudAdapter.eventBatch(device, eventBatch).get().getResponse();
    }

    /**
     * 设备批量上报属性
     *
     * @param device     设备信息
     * @param jsonObject 上报数据信息
     * @return 返回信息
     */
    private Response propertyBatchPub(Device device, JSONObject jsonObject)
            throws ExecutionException, InterruptedException {
        JSONArray data = jsonObject.getJSONArray(JsonResultUtil.DATA);
        //封装参数
        CheckArrayList<ParamsPropertyPub> paramsPropertyPubs = new CheckArrayList<>();
        for (Object entity : data) {
            String propertyPub = entity.toString();
            ParamsPropertyPub paramsPropertyPub = JSONObject.parseObject(propertyPub, ParamsPropertyPub.class);
            paramsPropertyPubs.add(paramsPropertyPub);
        }
        ParamsPropertyBatch paramsPropertyBatch = new ParamsPropertyBatch();
        paramsPropertyBatch.setData(paramsPropertyPubs);
        //上报平台 设备属性批量上报
        Response response = cloudAdapter.propertyBatch(device, paramsPropertyBatch).get().getResponse();
        logger.info("device property batch response:{}", response);
        return response;
    }

    /**
     * 网关子设备批量上报属性、事件
     *
     * @param gateway    网关设备信息
     * @param jsonObject 上报数据信息
     * @return 返回信息
     */
    private Response subDevicePropertyEventPub(Device gateway, List<Device> subDevicesList, JSONObject jsonObject)
            throws ExecutionException, InterruptedException {
        JSONObject data = jsonObject.getJSONObject(JsonResultUtil.DATA);

        JSONArray subServices = data.getJSONArray(JsonResultUtil.SUB_DEVICES);
        List<ParamsSubDevicePropertyEventPub> subDevices = new CheckArrayList<>();
        for (Object entity : subServices) {
            String propertyPub = entity.toString();
            ParamsSubDevicePropertyEventPub propertyEventPub =
                    JSONObject.parseObject(propertyPub, ParamsSubDevicePropertyEventPub.class);
            Device subDevice = deviceConfig.getDeviceEntity(propertyEventPub.getOriginalIdentity());
            propertyEventPub.setProductKey(subDevice.getProductKey());
            propertyEventPub.setDeviceKey(subDevice.getDeviceKey());
            subDevices.add(propertyEventPub);
        }
        //封装参数
        Request<SubDevicePropertyEventPub> request = new Request<>();
        SubDevicePropertyEventPub subDevicePropertyEventPub = new SubDevicePropertyEventPub();
        subDevicePropertyEventPub.setSubDevicePubs(subDevices);
        request.setParams(subDevicePropertyEventPub);
        //上报平台 网关子设备批量上报属性、事件
        Response response = cloudAdapter.propertyEventPub(gateway, request).get().getResponse();
        logger.info("subDevice logout response:{}", response);
        return response;
    }

    /**
     * 网关子设备添加topo
     *
     * @param gateway        网关设备信息
     * @param subDevice 子设备数据
     * @return 返回信息
     */
    private Response subDeviceAddTopo(Device gateway, Device subDevice)
            throws ExecutionException, InterruptedException {
        SubDeviceTopoUpdate.SubDeviceTopoUpdateParams subDeviceTopoUpdateParams = new SubDeviceTopoUpdate.SubDeviceTopoUpdateParams();
        subDeviceTopoUpdateParams.setDeviceId(subDevice.getDeviceId());
        subDeviceTopoUpdateParams.setDeviceKey(subDevice.getDeviceKey());
        subDeviceTopoUpdateParams.setProductKey(subDevice.getProductKey());
        subDeviceTopoUpdateParams.setSign(subDevice.getPassword());
        subDeviceTopoUpdateParams.setOriginalIdentity(subDevice.getOriginalIdentity());
        subDeviceTopoUpdateParams.setSignMethod(subDevice.getSignMethod());
        subDeviceTopoUpdateParams.setAuthType("0");

        Request<SubDeviceTopoUpdate.SubDeviceTopoUpdateParams> request = new Request<>();
        request.setParams(subDeviceTopoUpdateParams);
        //封装参数
        //上报平台
        Response response = cloudAdapter.subDeviceBindTopo(gateway, request).get().getResponse();
        logger.info("subDevice add topo response:{}", response);
        return response;
    }

    /**
     * 网关子设备删除topo
     *
     * @param gateway        网关设备信息
     * @param subDevice 子设备数据
     * @return 返回信息
     */
    private Response subDeviceDeleteTopo(Device gateway, Device subDevice)
            throws ExecutionException, InterruptedException {
        SubDeviceTopoUpdate subDeviceTopoUpdate = new SubDeviceTopoUpdate();
            SubDeviceTopoUpdate.SubDeviceTopoUpdateParams subDeviceTopoUpdateParams = new SubDeviceTopoUpdate.SubDeviceTopoUpdateParams();
            subDeviceTopoUpdateParams.setDeviceKey(subDevice.getDeviceKey());
            subDeviceTopoUpdateParams.setProductKey(subDevice.getProductKey());
            subDeviceTopoUpdateParams.setOriginalIdentity(subDevice.getOriginalIdentity());

        Request<SubDeviceTopoUpdate.SubDeviceTopoUpdateParams> request = new Request<>();
        request.setParams(subDeviceTopoUpdateParams);
        //封装参数
        //上报平台
        Response response = cloudAdapter.subDeviceDeleteTopo(gateway, request).get().getResponse();
        logger.info("subDevice delete topo response:{}", response);
        return response;
    }

    /**
     * 子设备下线
     *
     * @param gateway   网关设备信息
     * @param subDevice 网关子设备信息
     * @return 返回数据信息
     */
    private Response subDeviceLogout(Device gateway, Device subDevice) throws ExecutionException, InterruptedException {
        //上报平台 子设备下线
        Response response = cloudAdapter.subDeviceLogout(gateway, subDevice).getResponse();
        logger.info("subDevice logout response:{}", response);
        return response;
    }

    /**
     * 子设备上线
     *
     * @param gateway   网关设备信息
     * @param subDevice 子设备信息
     * @return 返回信息
     */
    private Response subDeviceLogin(Device gateway, Device subDevice) throws ExecutionException, InterruptedException {
        //上报平台 子设备上线
        Response response = cloudAdapter.subDeviceLogin(gateway, subDevice).get().getResponse();
        logger.info("subDevice login response:{}", response);
        return response;
    }

    /**
     * 设备上报事件
     *
     * @param device     设备信息
     * @param jsonObject 上报的事件信息
     * @return 返回信息
     */
    private Response eventPub(Device device, JSONObject jsonObject) throws ExecutionException, InterruptedException {
        //获取data中的事件信息
        JSONObject data = jsonObject.getJSONObject(JsonResultUtil.DATA);
        String key = data.get(JsonResultUtil.KEY).toString();
        JSONArray info = data.getJSONArray(JsonResultUtil.INFO);
        //封装请求参数
        EventData eventData = new EventData(key);
        for (Object jsonElement : info) {
            JSONObject js = JSONObject.parseObject(jsonElement.toString());
            String infoKey = js.getString(JsonResultUtil.KEY);
            Object infoValue = js.getObject(JsonResultUtil.VALUE, Object.class);
            eventData.addEvent(infoKey, infoValue);
        }
        //上报平台 设备事件上报
        Response response = cloudAdapter.eventPub(device, eventData).get().getResponse();
        logger.info("eventPub response:{}", response);
        return response;
    }

    /**
     * 设备上报属性
     *
     * @param device     设备信息
     * @param jsonObject 上报的属性信息
     * @return 返回信息
     */
    private Response propertyPub(Device device, JSONObject jsonObject) throws ExecutionException, InterruptedException {
        //获取data中的属性信息
        JSONObject data = jsonObject.getJSONObject(JsonResultUtil.DATA);
        String key = data.getString(JsonResultUtil.KEY);
        Object value = data.getObject(JsonResultUtil.VALUE, Object.class);
        if (key.isEmpty() || ObjectUtils.isEmpty(value)) {
            return new Response(null, ResultCode.KEY_VALUE_EXISTENT.getCode(),
                    ResultCode.KEY_VALUE_EXISTENT.getMessage());
        }
        //上报平台 设备上报属性
        CallableFuture<DeviceResult> callableResult = cloudAdapter.propertyPub(device, key, value);
        logger.info("propertyPub response为:{}", callableResult.get().getResponse());
        return callableResult.get().getResponse();
    }

    /**
     * 获取直连设备上线返回信息
     *
     * @param device 设备信息
     * @return 返回信息
     */
    private Response deviceLogin(Device device) throws ExecutionException, InterruptedException {
        //上报平台  设备上线
        CallableFuture<DeviceResult> deviceOnline = cloudAdapter.deviceLogin(device);
        DeviceResult result = deviceOnline.get();
        logger.info("device login response:{}", result.getResponse());
        return result.getResponse();
    }

    /**
     * 获取直连设备下线返回信息
     *
     * @param device 设备信息
     * @return 返回信息
     */
    private Response deviceLogout(Device device) throws ExecutionException, InterruptedException {
        //上报平台  设备下线
        DeviceResult result = cloudAdapter.deviceLogout(device);
        logger.info("device logout response:{}", result.getResponse());
        return result.getResponse();
    }

    /**
     * 网关子设备批量下线
     *
     * @param gateway    网关信息
     * @param subDevices 子设备集信息
     * @return 返回信息
     */
    private Response subDeviceLogoutBatch(Device gateway, List<Device> subDevices)
            throws ExecutionException, InterruptedException {
        //上报平台  子设备批量下线
        Response subResponse = cloudAdapter.subDeviceBatchLogout(gateway, subDevices).getResponse();
        logger.info("subDevice logout batch response:{}", subResponse);
        return subResponse;
    }

    /**
     * 网关子设备批量上线
     *
     * @param gateway    网关信息
     * @param subDevices 子设备集信息
     * @return 返回信息
     */
    private Response subDeviceLoginBatch(Device gateway, List<Device> subDevices)
            throws ExecutionException, InterruptedException {
        //上报平台  子设备批量上线
        CallableFuture<DeviceResult> deviceResultCallableFuture = cloudAdapter.subDeviceBatchLogin(gateway, subDevices);
        Response result = deviceResultCallableFuture.get().getResponse();
        logger.info("subDevice login batch response:{}", result);
        return result;
    }

    /**
     * @param device     设备信息
     * @param jsonObject 设备影子期望数据
     * @return返回信息
     */
    public Response deviceShadowUpdate(Device device, JSONObject jsonObject) {
        JSONObject data = jsonObject.getJSONObject(JsonResultUtil.DATA);
        JSONObject stateJsonObject = data.getJSONObject(JsonResultUtil.STATE);
        JSONObject desired = stateJsonObject.getJSONObject(JsonResultUtil.DESIRED);
        JSONObject reported = stateJsonObject.getJSONObject(JsonResultUtil.REPORTED);
        //封装请求参数
        Request<DeviceShadow> request = new Request<>();
        DeviceShadow deviceShadow = new DeviceShadow();
        State state = new State();
        state.setReported(reported);
        state.setDesired(desired);
        if (null != data.getString(JsonResultUtil.VERSION)) {
            deviceShadow.setVersion(Integer.valueOf(data.getString(JsonResultUtil.VERSION)));
        }
        deviceShadow.setState(state);
        request.setParams(deviceShadow);
        //更新设备影子
        Response response = null;
        try {
            response = cloudAdapter.deviceShadowUpdate(device, request).get().getResponse();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("deviceShadowUpdate login batch response:{}", response);
        return response;
    }

    /**
     * @param device 设备信息
     * @return 返回信息
     */
    public Response deviceShadowGet(Device device) {
        Response response = null;
        try {
            response = cloudAdapter.deviceShadowGet(device).get().getResponse();
        } catch (CancellationException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (AdapterException e) {
            e.printStackTrace();
        }
        logger.info("deviceShadowGet login batch response:{}", response);
        return response;
    }

    /**
     * 设备响应平台下发设备影子
     *
     * @param device
     * @param jsonObject
     * @return
     */
    public String deviceShadowCommandReply(Device device, JSONObject jsonObject) {
        JSONObject data = jsonObject.getJSONObject(JsonResultUtil.DATA);
        String messageId = data.getString(JsonResultUtil.MESSAGEID);
        String code = data.getString(JsonResultUtil.CODE);
        String message = data.getString(JsonResultUtil.MESSAGE);
        JSONArray requestData = null;
        if (null != data.getJSONArray(JsonResultUtil.DATA)) {
            requestData = data.getJSONArray(JsonResultUtil.DATA);
        }
        Response response = new Response(messageId, code, message, requestData);
        String result = cloudAdapter.deviceShadowCommandReply(device, response);
        return result;
    }
}
