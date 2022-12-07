package com.cuiot.openservices;

import com.cuiot.openservices.sdk.CloudAdapter;
import com.cuiot.openservices.sdk.config.ConfigFactory;
import com.cuiot.openservices.sdk.config.impl.AdapterFileConfig;
import com.cuiot.openservices.sdk.config.impl.DeviceFileConfig;
import com.cuiot.openservices.sdk.entity.CallableFuture;
import com.cuiot.openservices.sdk.entity.Device;
import com.cuiot.openservices.sdk.entity.DeviceResult;
import com.cuiot.openservices.sdk.entity.request.*;
import com.cuiot.openservices.sdk.handler.DefaultDownlinkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zhh
 * @date 2021/8/23 17:03
 * @description
 */
public class AppSubDevicePub {
    private static Logger logger = LoggerFactory.getLogger(AppSubDeviceBatch.class);

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

            // 子设备批量上线
            logger.info("### 子设备批量上线");
            List<Device> subDevices = new ArrayList<>();
            subDevices.add(deviceFileConfig.getDeviceEntity("self-fanSUBRUO001"));
            subDevices.add(deviceFileConfig.getDeviceEntity("self-fanSUBRUO002"));
            try {
                CallableFuture<DeviceResult> deviceLoginResult = cloudAdapter.subDeviceBatchLogin(gateway, subDevices);
                logger.info("response:{}", deviceLoginResult.get(10, TimeUnit.SECONDS).getResponse());
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 子设备上报属性，事件
            logger.info("### 子设备上报属性，事件");
            try {
                ParamsEventPub paramsEventPub = new ParamsEventPub();
                paramsEventPub.setKey("alarm");
                List<ParamsEventPub.EventItem> eventItems = new ArrayList<>();
                ParamsEventPub.EventItem eventItem = new ParamsEventPub.EventItem("ptalarm", 10);
                eventItems.add(eventItem);
                paramsEventPub.setInfo(eventItems);

                ParamsPropertyPub paramsPropertyPub = new ParamsPropertyPub();
                paramsPropertyPub.setKey("propertytest");
                paramsPropertyPub.setValue(20);

                SubDevicePropertyEventPub subDevice = new SubDevicePropertyEventPub();
                List<ParamsEventPub> paramsEventPubs = new ArrayList<>();
                paramsEventPubs.add(paramsEventPub);

                List<ParamsPropertyPub> paramsSubDevicePubs = new ArrayList<>();
                paramsSubDevicePubs.add(paramsPropertyPub);

                ParamsSubDevicePropertyEventPub paramsSubDevicePub = new ParamsSubDevicePropertyEventPub();
                paramsSubDevicePub.setDeviceKey("nxnapn3NUfp71Qi");
                paramsSubDevicePub.setProductKey("cu3ii4loz7azxum1");

                paramsSubDevicePub.setEvents(paramsEventPubs);
                paramsSubDevicePub.setProperties(paramsSubDevicePubs);

                List<ParamsSubDevicePropertyEventPub> pubs = new ArrayList<>();
                pubs.add(paramsSubDevicePub);
                subDevice.setSubDevicePubs(pubs);

                Request<SubDevicePropertyEventPub> request = new Request<>();
                request.setParams(subDevice);

                CallableFuture<DeviceResult> deviceLoginResult = cloudAdapter.propertyEventPub(gateway, request);
                logger.info("response:{}", deviceLoginResult.get(10, TimeUnit.SECONDS).getResponse());
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 子设备批量下线
            logger.info("### 子设备批量下线");
            try {
                CallableFuture<DeviceResult> deviceLogoutResult = cloudAdapter.subDeviceBatchLogout(gateway, subDevices);
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
