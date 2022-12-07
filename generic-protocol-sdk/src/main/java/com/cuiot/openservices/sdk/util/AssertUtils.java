package com.cuiot.openservices.sdk.util;

import com.cuiot.openservices.sdk.entity.ICheck;
import com.cuiot.openservices.sdk.entity.ReturnCode;
import com.cuiot.openservices.sdk.exception.AdapterException;

/**
 * @author yht
 */
public class AssertUtils {

    public static void check(ICheck... objs) throws AdapterException {
        if (objs == null || objs.length == 0) {
            return;
        }
        for (ICheck item : objs) {
            if (!item.checkLegal()) {
                throw new AdapterException(ReturnCode.DATA_ILLEGAL);
            }
        }
    }

}
