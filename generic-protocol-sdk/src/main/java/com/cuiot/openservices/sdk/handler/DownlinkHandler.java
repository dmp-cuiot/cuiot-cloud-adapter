package com.cuiot.openservices.sdk.handler;

import com.cuiot.openservices.sdk.entity.Device;
import com.cuiot.openservices.sdk.entity.request.ParamsPropertySet;
import com.cuiot.openservices.sdk.entity.request.ParamsServicePub;
import com.cuiot.openservices.sdk.entity.request.Request;
import com.cuiot.openservices.sdk.entity.shadow.DeviceShadow;
import com.cuiot.openservices.sdk.util.CheckArrayList;

/**
 * 下行处理器：包含平台下行请求，SDK内部下行回调通知等
 * 【注意】在回调通知中，不要做耗时操作，否则会影响SDK中业务线程的执行
 *
 * @author yht
 */
public interface DownlinkHandler {

    /**
     * 直连设备、网关：平台设置属性
     *
     * @param device  设备，包含productKey，deviceKey，originalIdentity
     * @param request 请求
     */
    void onPropertySetRequest(Device device, Request<CheckArrayList<ParamsPropertySet>> request);

    /**
     * 子设备：平台设置属性
     *
     * @param gateway   网关设备，包含productKey，deviceKey
     * @param subDevice 子设备，包含productKey，deviceKey，originalIdentity
     * @param request   请求
     */
    void onPropertySetRequest(Device gateway, Device subDevice, Request<CheckArrayList<ParamsPropertySet>> request);

    /**
     * 直连设备、网关：平台调用服务
     *
     * @param device  设备，包含productKey，deviceKey，originalIdentity
     * @param request 请求
     * @param sync    true:同步调用服务  false:异步调用服务
     */
    void onServicePubRequest(Device device, Request<ParamsServicePub> request, boolean sync);

    /**
     * 子设备：平台调用服务
     *
     * @param gateway   网关，包含productKey，deviceKey
     * @param subDevice 子设备，包含productKey，deviceKey，originalIdentity
     * @param request   请求
     * @param sync      true:同步调用服务  false:异步调用服务
     */
    void onServicePubRequest(Device gateway, Device subDevice, Request<ParamsServicePub> request, boolean sync);

    /**
     * 直连设备：平台下发设备影子
     *
     * @param device  设备
     * @param request 请求
     */
    void onDeviceShadowCommandRequest(Device device, Request<DeviceShadow> request);

    /**
     * 泛协议服务重连成功
     */
    void onServiceReconnected();

    /**
     * 泛协议服务断开连接
     */
    void onServiceDisconnected();
}
