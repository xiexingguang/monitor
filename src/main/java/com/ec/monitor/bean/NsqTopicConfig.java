package com.ec.monitor.bean;

/**
 * Created by jasshine_xxg on 2015/11/29.
 */
public class NsqTopicConfig {
    private String topicName;
    private int blockMsgThreshold;
    private int timeoutMsgThreshold;
    private int requeueMsgThreshold;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getBlockMsgThreshold() {
        return blockMsgThreshold;
    }

    public void setBlockMsgThreshold(int blockMsgThreshold) {
        this.blockMsgThreshold = blockMsgThreshold;
    }

    public int getTimeoutMsgThreshold() {
        return timeoutMsgThreshold;
    }

    public void setTimeoutMsgThreshold(int timeoutMsgThreshold) {
        this.timeoutMsgThreshold = timeoutMsgThreshold;
    }

    public int getRequeueMsgThreshold() {
        return requeueMsgThreshold;
    }

    public void setRequeueMsgThreshold(int requeueMsgThreshold) {
        this.requeueMsgThreshold = requeueMsgThreshold;
    }
}
