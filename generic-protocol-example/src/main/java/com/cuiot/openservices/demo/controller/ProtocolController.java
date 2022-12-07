package com.cuiot.openservices.demo.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cuiot.openservices.demo.config.ConfigLoadFactory;
import com.cuiot.openservices.demo.enums.ResultCode;
import com.cuiot.openservices.demo.service.AdapterService;
import com.cuiot.openservices.sdk.config.IAdapterConfig;
import com.cuiot.openservices.sdk.config.IDeviceConfig;
import com.cuiot.openservices.sdk.config.impl.AdapterFileConfig;
import com.cuiot.openservices.sdk.entity.Device;
import com.cuiot.openservices.sdk.entity.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author zhh
 * @date 2021/8/12 18:38
 * @description 示例代码接口
 */
@RestController
public class ProtocolController {

    private static final String TOKEN = "dmpiot";
    private static final String SUBORIGINALIDENTITYS = "subOriginalIdentitys";
//    @Autowired
//    private IDeviceConfig deviceConfig;

    @Autowired
    private IAdapterConfig adapterConfig;

    @Autowired
    private AdapterService service;

    /**
     * 直连设备上下线上报属性事件接口
     *
     * @param originalIdentity 配置文件设备信息前缀
     * @param data             格式如：
     *                         {
     *                         "type" : 3,
     *                         "data: {
     *                         "key" : "test01",
     *                         "value" : "20"
     *                         }
     *                         }
     * @return Response
     */
    @PostMapping("/deviceLogin")
    public Response deviceData(@RequestHeader("originalIdentity") String originalIdentity,
                               @RequestBody String data) {

        //获取配置文件中的设备信息=>通过originalIdentity
        IDeviceConfig deviceConfig = ConfigLoadFactory.getConfigPattern(adapterConfig.getConfigType());
        Device device = deviceConfig.getDeviceEntity(originalIdentity);
        if (device == null) {
            return new Response(null, ResultCode.DEVICE_EXISTENT.getCode(), ResultCode.DEVICE_EXISTENT.getMessage());
        }
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(data, JSONObject.class);
        } catch (Exception e) {
            return new Response(null, ResultCode.DATA_ILLEGAL.getCode(), ResultCode.DATA_ILLEGAL.getMessage());
        }
        return service.uploadDevicePub(device, jsonObject);
    }

    /**
     * 网关子设备单个上下线接口
     *
     * @param gatewayOriginalIdentity 配置文件网关设备名称
     * @param subOriginalIdentity     配置文件子设备名称
     * @param data                    data
     * @return Response
     */
    @PostMapping("subDeviceLogin")
    public Response subDeviceData(@RequestHeader("gatewayOriginalIdentity") String gatewayOriginalIdentity,
                                  @RequestHeader("subOriginalIdentity") String subOriginalIdentity,
                                  @RequestBody String data) {
        //获取配置文件中的设备信息=>通过originalIdentity
        IDeviceConfig deviceConfig = ConfigLoadFactory.getConfigPattern(adapterConfig.getConfigType());
        Device gateway = deviceConfig.getDeviceEntity(gatewayOriginalIdentity);
        if (gateway == null) {
            return new Response(null, ResultCode.DEVICE_EXISTENT.getCode(), ResultCode.DEVICE_EXISTENT.getMessage());
        }
        Device subDevice = deviceConfig.getDeviceEntity(subOriginalIdentity);
        if (subDevice == null) {
            return new Response(null, ResultCode.DEVICE_EXISTENT.getCode(), ResultCode.DEVICE_EXISTENT.getMessage());
        }
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(data, JSONObject.class);
        } catch (Exception e) {
            return new Response(null, ResultCode.DATA_ILLEGAL.getCode(), ResultCode.DATA_ILLEGAL.getMessage());
        }
        return service.uploadSubDevicePub(gateway, subDevice, jsonObject);
    }

    /**
     * 网关子设备批量上下线、属性上报和事件上报接口
     *
     * @param gatewayOriginalIdentity 配置文件设备信息前缀
     * @param data                    data
     * @return Response
     */
    @PostMapping("gatewaySubDeviceLogin")
    public Response gatewaySubDeviceData(@RequestHeader("gatewayOriginalIdentity") String gatewayOriginalIdentity,
                                         @RequestBody String data) {
        //获取配置文件中的设备信息=>通过originalIdentity
        IDeviceConfig deviceConfig = ConfigLoadFactory.getConfigPattern(adapterConfig.getConfigType());
        Device gateway = deviceConfig.getDeviceEntity(gatewayOriginalIdentity);
        if (gateway == null) {
            return new Response(null, ResultCode.DEVICE_EXISTENT.getCode(), ResultCode.DEVICE_EXISTENT.getMessage());
        }
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(data, JSONObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(null, ResultCode.DATA_ILLEGAL.getCode(), ResultCode.DATA_ILLEGAL.getMessage());
        }
        JSONArray subOriginalIdentitys = jsonObject.getJSONArray(SUBORIGINALIDENTITYS);
        List<Device> subDevices = new ArrayList<>();

        for (Object subOriginalIdentity : subOriginalIdentitys) {
            String originalIdentity = subOriginalIdentity.toString();
            Device subDevice = deviceConfig.getDeviceEntity(originalIdentity);
            subDevices.add(subDevice);
        }
        if (subDevices.isEmpty()) {
            return new Response(null, ResultCode.DEVICE_EXISTENT.getCode(), ResultCode.DEVICE_EXISTENT.getMessage());
        }
        return service.uploadSubDeviceBatch(gateway, subDevices, jsonObject);
    }

    /**
     * 网关子设备修改拓扑关系
     *
     * @param gatewayOriginalIdentity 配置文件设备信息前缀
     * @param data                    data
     * @return Response
     */
    @PostMapping("gatewaySubUpdateTopo")
    public Response gatewaySubDeviceModifyTopo(@RequestHeader("gatewayOriginalIdentity") String gatewayOriginalIdentity,
                                               @RequestBody String data) throws ExecutionException, InterruptedException {
        //获取配置文件中的设备信息=>通过originalIdentity
        IDeviceConfig deviceConfig = ConfigLoadFactory.getConfigPattern(adapterConfig.getConfigType());
        Device gateway = deviceConfig.getDeviceEntity(gatewayOriginalIdentity);
        if (gateway == null) {
            return new Response(null, ResultCode.DEVICE_EXISTENT.getCode(), ResultCode.DEVICE_EXISTENT.getMessage());
        }
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(data, JSONObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(null, ResultCode.DATA_ILLEGAL.getCode(), ResultCode.DATA_ILLEGAL.getMessage());
        }
        String originalIdentity = jsonObject.getString(SUBORIGINALIDENTITYS);
        List<Device> subDevices = new ArrayList<>();

        Device subDevice = deviceConfig.getDeviceEntity(originalIdentity);
        subDevices.add(subDevice);

        return service.uploadSubDeviceBatch(gateway, subDevices, jsonObject);
    }

    /**
     * 设备更新设备影子
     *
     * @param originalIdentity 配置文件设备信息前缀
     * @param data             data
     * @return
     */
    @PostMapping("/shadowUpdate")
    public Response deviceShadowUpdate(@RequestHeader("originalIdentity") String originalIdentity,
                                       @RequestBody String data) {
        //获取配置文件中的设备信息=>通过originalIdentity
        IDeviceConfig deviceConfig = ConfigLoadFactory.getConfigPattern(adapterConfig.getConfigType());
        Device device = deviceConfig.getDeviceEntity(originalIdentity);
        if (device == null) {
            return new Response(null, ResultCode.DEVICE_EXISTENT.getCode(), ResultCode.DEVICE_EXISTENT.getMessage());
        }
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(data, JSONObject.class);
        } catch (Exception e) {
            return new Response(null, ResultCode.DATA_ILLEGAL.getCode(), ResultCode.DATA_ILLEGAL.getMessage());
        }
        return service.deviceShadowUpdate(device, jsonObject);
    }

    /**
     * 获取设备影子
     *
     * @param originalIdentity 配置文件设备信息前缀
     * @return
     */
    @PostMapping("/shadowGet")
    public Response deviceShadowGet(@RequestHeader("originalIdentity") String originalIdentity) {
        //获取配置文件中的设备信息=>通过originalIdentity
        IDeviceConfig deviceConfig = ConfigLoadFactory.getConfigPattern(adapterConfig.getConfigType());
        Device device = deviceConfig.getDeviceEntity(originalIdentity);
        if (device == null) {
            return new Response(null, ResultCode.DEVICE_EXISTENT.getCode(), ResultCode.DEVICE_EXISTENT.getMessage());
        }
        return service.deviceShadowGet(device);
    }

    /**
     * 设备响应平台下发设备影子
     *
     * @param originalIdentity 配置文件设备信息前缀
     * @param data             data
     * @return
     */
    @PostMapping("/shadowCommandReply")
    public String deviceShadowCommandReply(@RequestHeader("originalIdentity") String originalIdentity,
                                           @RequestBody String data) {
        //获取配置文件中的设备信息=>通过originalIdentity
        IDeviceConfig deviceConfig = ConfigLoadFactory.getConfigPattern(adapterConfig.getConfigType());
        Device device = deviceConfig.getDeviceEntity(originalIdentity);
        if (device == null) {
            return new Response(null, ResultCode.DEVICE_EXISTENT.getCode(), ResultCode.DEVICE_EXISTENT.getMessage()).toString();
        }
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(data, JSONObject.class);
        } catch (Exception e) {
            return new Response(null, ResultCode.DATA_ILLEGAL.getCode(), ResultCode.DATA_ILLEGAL.getMessage()).toString();
        }
        return service.deviceShadowCommandReply(device, jsonObject);
    }
}
