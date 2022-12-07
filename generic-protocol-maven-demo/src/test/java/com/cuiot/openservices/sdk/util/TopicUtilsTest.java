package com.cuiot.openservices.sdk.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TopicUtilsTest {

    @Test
    public void test() {
        String topic = "$proxy/933538275464156/cu68kpjrahs6wasU/fanZLDev001/device/login_reply";
        String ret = TopicUtils.getPostfixFromTopic(topic);
        System.out.print("ret:" + ret);
    }

}