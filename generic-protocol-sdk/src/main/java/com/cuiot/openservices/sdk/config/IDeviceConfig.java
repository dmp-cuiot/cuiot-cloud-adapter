package com.cuiot.openservices.sdk.config;

import com.cuiot.openservices.sdk.entity.Device;

import java.util.List;

/**
 * 设备配置接口类，默认不使用
 *
 * @author yht
 */
public interface IDeviceConfig {

    /**
     * 获取平台设备相关信息
     *
     * @param originalIdentity 每个设备的唯一识别字符串
     * @return Device
     */
    Device getDeviceEntity(String originalIdentity);

    /**
     * 获所有的设备相关信息
     * @return
     */
    List<Device> getAllDevices();

    /**
     * 获取设备唯一识别字符串
     *
     * @param productKey 产品Key
     * @param deviceKey  设备Key
     * @return String
     */
    String getOriginalIdentity(String productKey, String deviceKey);

    /**
     * 向配置文件写入数据
     *
     * @param device 设备实体
     * @return boolean
     */
    boolean setDeviceEntity(Device device);

    /**
     * 批量向配置文件写入数据
     *
     * @param devices 设备集合
     * @return boolean
     */
    boolean setDeviceEntitys(List<Device> devices);

    /**
     * 通过设备唯一编号、产品key、设备key删除配置库的设备配置
     * @param originalIdentity
     * @return
     */
    boolean deleteDeviceEntity(String originalIdentity, String productKey, String deviceKey);

}
