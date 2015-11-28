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
    public String nsqInteval;

    @Value("${nsq.msgthreshold}")
    public String needMonitorTopic;



}
