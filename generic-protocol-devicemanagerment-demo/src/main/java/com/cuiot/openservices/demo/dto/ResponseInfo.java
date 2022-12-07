package com.cuiot.openservices.demo.dto;

import com.cuiot.openservices.sdk.entity.Device;
import lombok.Data;

@Data
public class ResponseInfo {

    private String code;
    private Device data;
    private String requestId;
    private String message;
}
