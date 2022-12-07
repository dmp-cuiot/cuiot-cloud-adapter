package com.cuiot.openservices.sdk.util;

import com.cuiot.openservices.sdk.constant.ServerCode;

/**
 * @author wanghy
 */
public class CheckUtils {

    public static boolean isDevicesReachedLimit(String code) {
        return ServerCode.SERVICE_ACCESS_NUMBER_REACH_MAXIMUM.getCode().equals(code);
    }
}
