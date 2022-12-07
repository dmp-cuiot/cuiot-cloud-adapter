package com.cuiot.openservices.demo.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DataParam {

    @JSONField(name = "app_id")
    private String appId;

    private String timestamp;

    @JSONField(name = "trans_id")
    private String transId;

    private String token;

    private Object data;
}
