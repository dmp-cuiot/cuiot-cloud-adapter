package com.cuiot.openservices;

import com.cuiot.openservices.sdk.CloudAdapter;
import com.cuiot.openservices.sdk.config.ConfigFactory;
import com.cuiot.openservices.sdk.config.impl.AdapterFileConfig;
import com.cuiot.openservices.sdk.config.impl.DeviceFileConfig;
import com.cuiot.openservices.sdk.entity.CallableFuture;
import com.cuiot.openservices.sdk.entity.Device;
import com.cuiot.openservices.sdk.entity.DeviceResult;
import com.cuiot.openservices.sdk.entity.request.EventData;
import com.cuiot.openservices.sdk.entity.request.ParamsPropertyPub;
import com.cuiot.openservices.sdk.entity.request.Request;
import com.cuiot.openservices.sdk.handler.DefaultDownlinkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 网关：上下线、上报属性、上报事件
 *
 * @author yht
 */
public class AppGateway {
    private static Logger logger = LoggerFactory.getLogger(AppGateway.class);

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

            // 网关上报属性
            logger.info("### 网关上报属性");
            try {
                Request<ParamsPropertyPub> request = new Request<>();
                request.setParams(new ParamsPropertyPub("radiatorTemperature", 15));
                CallableFuture<DeviceResult> future = cloudAdapter.propertyPub(gateway, "radiatorTemperature0",15);
                logger.info("response:{}", future.get(5, TimeUnit.SECONDS).getResponse());
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 网关上报事件
            logger.info("### 网关上报事件");
            try {
                EventData eventData = new EventData("testEvent3");
                eventData.addEvent("test1",111);
                eventData.addEvent("test2",111);
                CallableFuture<DeviceResult> future = cloudAdapter.eventPub(gateway, eventData);
                logger.info("response:{}", future.get(5, TimeUnit.SECONDS).getResponse());
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
