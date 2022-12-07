package com.cuiot.openservices.sdk.util;

import com.cuiot.openservices.sdk.mqtt.promise.DevicePromise;
import com.cuiot.openservices.sdk.entity.DeviceResult;
import com.cuiot.openservices.sdk.entity.ReturnCode;

import java.util.Collection;

/**
 * @author yht
 */
public class Utils {

    public static DevicePromise<DeviceResult> createResultCallback(ReturnCode returnCode) {
        DevicePromise<DeviceResult> promise = new DevicePromise<>();
        promise.trySuccess(new DeviceResult(returnCode));
        return promise;
    }
    public static DeviceResult createResult(ReturnCode returnCode) {
        return new DeviceResult(returnCode);
    }

    public static DevicePromise<DeviceResult> createResult(String id, ReturnCode returnCode) {
        DevicePromise<DeviceResult> promise = new DevicePromise<>();
        promise.trySuccess(new DeviceResult(id, returnCode));
        return promise;
    }

    public static DevicePromise<DeviceResult> createFailureResult(String message) {
        DevicePromise<DeviceResult> promise = new DevicePromise<>();
        promise.tryFailure(new Exception(message));
        return promise;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

}
