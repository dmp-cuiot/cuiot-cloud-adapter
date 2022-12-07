package com.cuiot.openservices.demo.handler;

import com.alibaba.fastjson.JSONObject;
import com.cuiot.openservices.sdk.CloudAdapter;
import com.cuiot.openservices.sdk.config.impl.DeviceFileConfig;
import com.cuiot.openservices.sdk.entity.Device;
import com.cuiot.openservices.sdk.entity.request.Request;
import com.cuiot.openservices.sdk.entity.response.Response;
import com.cuiot.openservices.sdk.entity.shadow.DeviceShadow;
import com.cuiot.openservices.sdk.handler.DefaultDownlinkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description：处理平台下发设备影子回复消息
 * @Author: wanghy
 * @Date: 2021/10/20 14:45
 */
@Component
public class AutoDownLinkHandler extends DefaultDownlinkHandler {

    private final Logger logger = LoggerFactory.getLogger(AutoDownLinkHandler.class);

    @Autowired
    private CloudAdapter cloudAdapter;

    @Override
    public void onDeviceShadowCommandRequest(Device device, Request<DeviceShadow> request) {
        DeviceFileConfig deviceConfig = new DeviceFileConfig();
        Device newDevice = deviceConfig.getDeviceEntity(device.getOriginalIdentity());
        String messageId = request.getMessageId();
        DeviceShadow shadow = request.getParams();
        JSONObject desired = shadow.getState().getDesired();
        Response response = new Response(messageId, "000000", "success", desired);
        String resultMessageId = cloudAdapter.deviceShadowCommandReply(newDevice, response);
        logger.info("deviceShadowCommandReply messageId:{}", resultMessageId);
    }
}
