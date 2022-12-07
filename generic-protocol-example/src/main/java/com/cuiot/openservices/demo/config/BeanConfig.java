package com.cuiot.openservices.demo.config;

import com.cuiot.openservices.sdk.CloudAdapter;
import com.cuiot.openservices.sdk.config.IAdapterConfig;
import com.cuiot.openservices.sdk.config.IDeviceConfig;
import com.cuiot.openservices.sdk.config.impl.AdapterFileConfig;
import com.cuiot.openservices.sdk.config.impl.DeviceFileConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yht
 */
@Configuration
public class BeanConfig {
    @Bean
    public IAdapterConfig adapterConfig() {
        IAdapterConfig adapterConfig = new AdapterFileConfig();
        return adapterConfig;
    }

    @Bean
    public IDeviceConfig deviceConfig() {
        IDeviceConfig deviceConfig = new DeviceFileConfig();
        return deviceConfig;
    }

    @Bean
    public CloudAdapter cloudAdapter() {
        return new CloudAdapter();
    }
}
