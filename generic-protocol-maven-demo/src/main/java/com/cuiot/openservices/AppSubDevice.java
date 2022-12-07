package com.cuiot.openservices;

import com.cuiot.openservices.sdk.CloudAdapter;
import com.cuiot.openservices.sdk.config.ConfigFactory;
import com.cuiot.openservices.sdk.config.impl.AdapterFileConfig;
import com.cuiot.openservices.sdk.config.impl.DeviceFileConfig;
import com.cuiot.openservices.sdk.entity.CallableFuture;
import com.cuiot.openservices.sdk.entity.Device;
import com.cuiot.openservices.sdk.entity.DeviceResult;
import com.cuiot.openservices.sdk.entity.request.ParamsServicePub;
import com.cuiot.openservices.sdk.entity.response.SubDeviceCmdResponse;
import com.cuiot.openservices.sdk.handler.DefaultDownlinkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 子设备：上下线、上报属性、上报事件、响应设置属性、响应调用服务
 *
 * @author yht
 */
public class AppSubDevice {
    private static Logger logger = LoggerFactory.getLogger(AppSubDevice.class);

    public static void main(String[] args) {
        try {
            AdapterFileConfig adapterFileConfig = new AdapterFileConfig();
            DeviceFileConfig deviceFileConfig = new DeviceFileConfig();
            // 初始化配置信息
            ConfigFactory.init(adapterFileConfig, deviceFileConfig);
            CloudAdapter cloudAdapter = new CloudAdapter();
            // 下行处理
            cloudAdapter.setDownlinkHandler(new DefaultDownlinkHandler());
            // 启动：包含泛协议上线，订阅topic
            cloudAdapter.start();

            // 网关上线
            logger.info("### 网关上线");
            Device gateway = deviceFileConfig.getDeviceEntity("self-fanWG001");
            try {
                CallableFuture<DeviceResult> deviceLoginResult = cloudAdapter.deviceLogin(gateway);
                logger.info("response:{}", deviceLoginResult.get(10, TimeUnit.SECONDS).getResponse());
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 子设备上线
            logger.info("### 子设备上线");
            Device subDevice = deviceFileConfig.getDeviceEntity("self-fanSUBRUO001");
            try {
                CallableFuture<DeviceResult> deviceLoginResult = cloudAdapter.subDeviceLogin(gateway, subDevice);
                logger.info("response:{}", deviceLoginResult.get(10, TimeUnit.SECONDS).getResponse());
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 子设备响应设置属性
            logger.info("### 子设备响应设置属性");
            try {
                SubDeviceCmdResponse response = new SubDeviceCmdResponse<>("123", "000000", "success", subDevice);
                String messageId = cloudAdapter.propertySetReply(gateway, response);
                logger.info("response messageId:{}", messageId);
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 子设备响应下发服务
            logger.info("### 子设备响应下发服务");
            try {
                List<ParamsServicePub.Info> data = new ArrayList<>();
                data.add(new ParamsServicePub.Info("test1", 29));
                SubDeviceCmdResponse<List<ParamsServicePub.Info>> response = new SubDeviceCmdResponse<>("123", "000000", "success", subDevice, data);
                String messageId = cloudAdapter.servicePubReply(gateway, response, true);
                logger.info("response messageId:{}", messageId);
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 子设备下线
            logger.info("### 子设备下线");
            try {
                CallableFuture<DeviceResult> deviceLogoutResult = cloudAdapter.subDeviceLogout(gateway, subDevice);
                logger.info("response:{}", deviceLogoutResult.get(5, TimeUnit.SECONDS).getResponse());
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 网关下线
            logger.info("### 网关下线");
            try {
                CallableFuture<DeviceResult> deviceLogoutResult = cloudAdapter.deviceLogout(gateway);
                logger.info("response:{}", deviceLogoutResult.get(5, TimeUnit.SECONDS).getResponse());
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 泛协议下线
            cloudAdapter.stop();
        } catch (Exception e) {
            logger.error("exception", e);
        }

        try {
            System.in.read();
        } catch (IOException e) {
            logger.error("exception", e);
        }
    }
}
