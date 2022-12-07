package com.cuiot.openservices;

import com.cuiot.openservices.sdk.CloudAdapter;
import com.cuiot.openservices.sdk.config.ConfigFactory;
import com.cuiot.openservices.sdk.config.impl.AdapterFileConfig;
import com.cuiot.openservices.sdk.config.impl.DeviceFileConfig;
import com.cuiot.openservices.sdk.entity.CallableFuture;
import com.cuiot.openservices.sdk.entity.Device;
import com.cuiot.openservices.sdk.entity.DeviceResult;
import com.cuiot.openservices.sdk.handler.DefaultDownlinkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 泛协议服务断连
 *
 * @author yht
 */
public class AppServiceDisconnect {
    private static Logger logger = LoggerFactory.getLogger(AppServiceDisconnect.class);

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
            cloudAdapter.stop();
            cloudAdapter.start();
            cloudAdapter.stop();

            cloudAdapter.start();

            // 直连设备上线
            logger.info("### 直连设备上线");
            Device device = deviceFileConfig.getDeviceEntity("self-fanZLDev001");
            try {
                CallableFuture<DeviceResult> deviceLoginResult = cloudAdapter.deviceLogin(device);
                logger.info("response:{}", deviceLoginResult.get(10, TimeUnit.SECONDS).getResponse());
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 直连设备上报属性
            logger.info("### 直连设备上报属性");
            try {
                CallableFuture<DeviceResult> future = cloudAdapter.propertyPub(device, "test",15);
                logger.info("response:{}", future.get(5, TimeUnit.SECONDS).getResponse());
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 直连设备下线
            logger.info("### 直连设备下线");
            try {
                CallableFuture<DeviceResult> deviceLogoutResult = cloudAdapter.deviceLogout(device);
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
