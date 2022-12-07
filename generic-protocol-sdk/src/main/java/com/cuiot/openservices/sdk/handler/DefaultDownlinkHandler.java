package com.cuiot.openservices.sdk.handler;

import com.cuiot.openservices.sdk.entity.Device;
import com.cuiot.openservices.sdk.entity.request.ParamsPropertySet;
import com.cuiot.openservices.sdk.entity.request.ParamsServicePub;
import com.cuiot.openservices.sdk.entity.request.Request;
import com.cuiot.openservices.sdk.entity.shadow.DeviceShadow;
import com.cuiot.openservices.sdk.util.CheckArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认下行处理器
 *
 * @author yht
 */
public  class DefaultDownlinkHandler implements DownlinkHandler {

    private final Logger logger = LoggerFactory.getLogger(DefaultDownlinkHandler.class);

    @Override
    public void onPropertySetRequest(Device device, Request<CheckArrayList<ParamsPropertySet>> request) {
        logger.info("onPropertySetRequest, device:{} request:{}", device, request);
    }

    @Override
    public void onPropertySetRequest(Device gateway, Device subDevice, Request<CheckArrayList<ParamsPropertySet>> request) {
        logger.info("onPropertySetRequest, gateway:{} subDevice:{} request:{}", gateway, subDevice, request);
    }

    @Override
    public void onServicePubRequest(Device device, Request<ParamsServicePub> request, boolean sync) {
        logger.info("onServicePubRequest, device:{} request:{} sync:{}", device, request, sync);
    }

    @Override
    public void onServicePubRequest(Device gateway, Device subDevice, Request<ParamsServicePub> request, boolean sync) {
        logger.info("onServicePubRequest, gateway:{} subDevice:{} request:{} sync:{}", gateway, subDevice, request, sync);
    }

    @Override
    public void onDeviceShadowCommandRequest(Device device, Request<DeviceShadow> request) {
        logger.info("onDeviceShadowCommandRequest, device:{} request:{}", device, request);
    }

    @Override
    public void onServiceReconnected() {
        logger.info("service reconnected");
    }

    @Override
    public void onServiceDisconnected() {
        logger.warn("service disconnected");
    }

}