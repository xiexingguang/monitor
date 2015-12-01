package com.ec.monitor.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by ecuser on 2015/11/25.
 */

@Component
public class NsqwatcherProperties {

    @Value("${nsq.lookup}")
    public  String lookupUrl;

    @Value("${nsq.monitor.interval}")
    public long nsqInteval;

    @Value("${nsq.needMonitorTopic}")
    public String needMonitorTopic;

    @Value("${nsq.monitor.all.topic}")
    public String monitorAllTopic;

    @Value("${nsq.default.blockmsg}")
    public int defaultTopicThreshold;

    @Value("${nsq.default.timeoutmsg}")
    public int defaultTimeoutThreshold;

    @Value("${nsq.default.requeuemsg}")
    public int defaultRequeThreshold;



}
