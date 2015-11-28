package com.ec.monitor.bean;

import java.util.List;

/**
 * Created by ecuser on 2015/11/26.
 */
public class NsqProducerBean {

    private String url;
    private int port;
    private String getNodesStatsUrl;
    private String host;

    public List<NsqTopicBean> getNsqTopicBeans() {
        return nsqTopicBeans;
    }

    public NsqProducerBean setNsqTopicBeans(List<NsqTopicBean> nsqTopicBeans) {
        this.nsqTopicBeans = nsqTopicBeans;
        return this;
    }

    private List<NsqTopicBean> nsqTopicBeans;

    public String getHost() {
        return url + ":" + port;
    }

    public String getGetNodesStatsUrl() {
        return "http://" + url + ":" + port + "/stats";
    }

    public String getUrl() {
        return url;
    }

    public NsqProducerBean setUrl(String url) {
        this.url = url;
        return this;
    }

    public int getPort() {
        return port;
    }

    public NsqProducerBean setPort(int port) {
        this.port = port;
        return this;
    }


}
