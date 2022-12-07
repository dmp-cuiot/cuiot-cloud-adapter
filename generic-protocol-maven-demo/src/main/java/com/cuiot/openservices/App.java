package com.cuiot.openservices;

import com.cuiot.openservices.handler.AutoDownLinkHandler;
import com.cuiot.openservices.sdk.CloudAdapter;
import com.cuiot.openservices.sdk.config.ConfigFactory;
import com.cuiot.openservices.sdk.config.impl.AdapterFileConfig;
import com.cuiot.openservices.sdk.config.impl.DeviceFileConfig;
import com.cuiot.openservices.sdk.entity.CallableFuture;
import com.cuiot.openservices.sdk.entity.Device;
import com.cuiot.openservices.sdk.entity.DeviceResult;
import com.cuiot.openservices.sdk.entity.request.*;
import com.cuiot.openservices.sdk.entity.response.Response;
import com.cuiot.openservices.sdk.util.CheckArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 直连设备：上下线、上报属性、上报事件、响应设置属性、响应调用服务
 *
 * @author yht
 */
public class App {
    private static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        try {
            AdapterFileConfig adapterFileConfig = new AdapterFileConfig();
            DeviceFileConfig deviceFileConfig = new DeviceFileConfig();
            // 初始化配置信息
            ConfigFactory.init(adapterFileConfig, deviceFileConfig);
            CloudAdapter cloudAdapter = new CloudAdapter();
            // 下行处理
            cloudAdapter.setDownlinkHandler(new AutoDownLinkHandler(cloudAdapter));
            // 启动：包含泛协议上线，订阅topic
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

            // 直连设备批量上报属性
            logger.info("### 直连设备批量上报属性");
            try {
                ParamsPropertyBatch paramsPropertyBatch = new ParamsPropertyBatch();
                CheckArrayList<ParamsPropertyPub> data = new CheckArrayList<>();
                data.add(new ParamsPropertyPub("radiatorTemperature", 19));
                data.add(new ParamsPropertyPub("outputVoltage", 200));
                data.add(new ParamsPropertyPub("test", 13));
                paramsPropertyBatch.setData(data);
                CallableFuture<DeviceResult> future = cloudAdapter.propertyBatch(device, paramsPropertyBatch);
                logger.info("response:{}", future.get(5, TimeUnit.SECONDS).getResponse());
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 直连设备上报事件
            logger.info("### 直连设备上报事件");
            try {
                EventData eventData = new EventData("testEvent100");
                eventData.addEvent("param1",156);
                eventData.addEvent("param2", 269);
                CallableFuture<DeviceResult> future = cloudAdapter.eventPub(device, eventData);
                logger.info("response:{}", future.get(5, TimeUnit.SECONDS).getResponse());
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 直连设备批量上报事件
            logger.info("### 直连设备批量上报事件");
            try {
                EventBatch eventBatch  = new EventBatch();
                EventData eventData1 = new EventData("testEvent3");
                eventData1.addEvent("param1",156);
                eventData1.addEvent("param2", 269);
                EventData eventData2 = new EventData("testEvent100");
                eventData2.addEvent("param1", 156);
                eventData2.addEvent("param2", 269);
                eventBatch.add(eventData1);
                eventBatch.add(eventData2);
                CallableFuture<DeviceResult> future = cloudAdapter.eventBatch(device, eventBatch);
                logger.info("response:{}", future.get(5, TimeUnit.SECONDS).getResponse());
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 直连设备响应设置属性
            logger.info("### 直连设备响应设置属性");
            try {
                Response response = new Response("123", "000000", "success");
                String messageId = cloudAdapter.propertySetReply(device, response);
                logger.info("response messageId:{}", messageId);
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 直连设备响应下发服务
            logger.info("### 直连设备响应下发服务");
            try {
                List<ParamsServicePub.Info> data = new ArrayList<>();
                data.add(new ParamsServicePub.Info("test1", 29));
                Response<List<ParamsServicePub.Info>> response = new Response("123", "000000", "success", data);
                response.setData(data);
                String messageId = cloudAdapter.servicePubReply(device, response, true);
                logger.info("response messageId:{}", messageId);
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 直连设备下线
            logger.info("### 直连设备下线");
            try {
                DeviceResult deviceLogoutResult = cloudAdapter.deviceLogout(device);
                logger.info("response:{}", deviceLogoutResult.getResponse());
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
