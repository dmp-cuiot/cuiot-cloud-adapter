package com.cuiot.openservices;

import com.alibaba.fastjson.JSONObject;
import com.cuiot.openservices.handler.AutoDownLinkHandler;
import com.cuiot.openservices.sdk.CloudAdapter;
import com.cuiot.openservices.sdk.config.ConfigFactory;
import com.cuiot.openservices.sdk.config.impl.AdapterFileConfig;
import com.cuiot.openservices.sdk.config.impl.DeviceFileConfig;
import com.cuiot.openservices.sdk.entity.CallableFuture;
import com.cuiot.openservices.sdk.entity.Device;
import com.cuiot.openservices.sdk.entity.DeviceResult;
import com.cuiot.openservices.sdk.entity.request.Request;
import com.cuiot.openservices.sdk.entity.shadow.DeviceShadow;
import com.cuiot.openservices.sdk.entity.shadow.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 直连设备：上下线、上报属性、上报事件、响应设置属性、响应调用服务
 *
 * @author yht
 */
public class AppDeviceShadow {
    private static Logger logger = LoggerFactory.getLogger(AppDeviceShadow.class);

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
            Device device = deviceFileConfig.getDeviceEntity("originalIdentity");
            try {
                CallableFuture<DeviceResult> deviceLoginResult = cloudAdapter.deviceLogin(device);
                logger.info("response:{}", deviceLoginResult.get(10, TimeUnit.SECONDS).getResponse());
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 直连设备更新设备影子
            logger.info("### 直连设备更新设备影子");
            try {
                Request<DeviceShadow> request = new Request<>();
                DeviceShadow ds = new DeviceShadow();
                State state = new State();
                JSONObject reported = new JSONObject();
                reported.put("testInt", 2);
                state.setReported(reported);
                ds.setState(state);
                request.setParams(ds);
                CallableFuture<DeviceResult> future = cloudAdapter.deviceShadowUpdate(device, request);
                logger.info("response:{}", future.get(5, TimeUnit.SECONDS).getResponse());
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 直连设备获取设备影子
            logger.info("### 直连设备获取设备影子");
            try {
                CallableFuture<DeviceResult> future = cloudAdapter.deviceShadowGet(device);
                logger.info("response:{}", future.get(5, TimeUnit.SECONDS).getResponse());
            } catch (Exception e) {
                logger.error("exception", e);
            }

            // 直连设备响应平台下发设备影子
//            logger.info("### 直连设备响应平台下发设备影子");
//            try {
//                Response response = new Response("123", "000000", "success");
//                String messageId = cloudAdapter.deviceShadowCommandReply(device, response);
//                logger.info("response messageId:{}", messageId);
//            } catch (Exception e) {
//                logger.error("exception", e);
//            }

//            // 直连设备下线
//            logger.info("### 直连设备下线");
//            try {
//                CallableFuture<DeviceResult> deviceLogoutResult = cloudAdapter.deviceLogout(device);
//                logger.info("response:{}", deviceLogoutResult.get(5, TimeUnit.SECONDS).getResponse());
//            } catch (Exception e) {
//                logger.error("exception", e);
//            }
//
//            // 泛协议下线
//            cloudAdapter.stop();
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
