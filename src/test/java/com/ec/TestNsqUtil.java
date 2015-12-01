package com.ec;

import com.ec.monitor.nsq.NsqUtil;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ecuser on 2015/11/30.
 */
public class TestNsqUtil extends TestCase {

    public void testGetProducers() {
        NsqUtil.getNsqproducers("http://10.0.200.51:1175/nodes");
    }

    public void testGetNsqdinfoByNsqdURL() {
        NsqUtil.generateNsqdNodeInfoByNsqdUrl("http://10.0.200.51:1176");
    }


    //TODO,测试获取所有集群环境中的节点topic信息
    public void testGetClusterTopic() {
        List<String> urls = new ArrayList<>();

    }

    public void testMsgTopicThresholdMapping() {
        String config = "java:124,crm:24:14:1545,take,good:1314:41341";
        NsqUtil.msgTopicThresholdMapping(config, 2000, 2000, 2000); //目前topic 没有去重校验判断
        /**
         * /**
         * {
         "take": {
         "blockMsgThreshold": 2000,
         "requeueMsgThreshold": 2000,
         "timeoutMsgThreshold": 2000
         },
         "crm": {
         "blockMsgThreshold": 24,
         "requeueMsgThreshold": 1545,
         "timeoutMsgThreshold": 14
         },
         "good": {
         "blockMsgThreshold": 1314,
         "requeueMsgThreshold": 2000,
         "timeoutMsgThreshold": 41341
         },
         "java": {
         "blockMsgThreshold": 124,
         "requeueMsgThreshold": 2000,
         "timeoutMsgThreshold": 2000
         }
         }
         */




    }


}
