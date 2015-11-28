package com.ec.monitor.bean;

import java.util.List;

/**
 * Created by ecuser on 2015/11/26.
 */
public class NsqTopicBean {

    private String topicName;
    private List<NsqChannelBean> channelBeans;
    private int depth;
    private int backend_depth;
    private int message_count;

    public int getDepth() {
        return depth;
    }

    public NsqTopicBean setDepth(int depth) {
        this.depth = depth;
        return this;
    }

    public int getMessage_count() {
        return message_count;
    }

    public NsqTopicBean setMessage_count(int message_count) {
        this.message_count = message_count;
        return this;
    }

    public int getBackend_depth() {
        return backend_depth;
    }

    public NsqTopicBean setBackend_depth(int backend_depth) {
        this.backend_depth = backend_depth;
        return this;
    }

    public String getTopicName() {
        return topicName;
    }

    public NsqTopicBean setTopicName(String topicName) {
        this.topicName = topicName;
        return this;
    }

    public List<NsqChannelBean> getChannelBeans() {
        return channelBeans;
    }

    public NsqTopicBean setChannelBeans(List<NsqChannelBean> channelBeans) {
        this.channelBeans = channelBeans;
        return this;
    }
}
