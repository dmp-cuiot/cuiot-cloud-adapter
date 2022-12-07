package com.cuiot.openservices.sdk.util;

import com.cuiot.openservices.sdk.entity.ICheck;

import java.util.ArrayList;

/**
 * @author yht
 */
public class CheckArrayList<T> extends ArrayList<T> implements ICheck {

    @Override
    public boolean checkLegal() {
        // TODO 可以继续判断item是否为ICheck类型，并校验
        return !this.isEmpty();
    }

}
