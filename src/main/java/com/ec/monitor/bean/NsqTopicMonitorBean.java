package com.ec.monitor.bean;

/**
 * Created by ecuser on 2015/11/27.
 */
public class NsqTopicMonitorBean extends AbstractNsqMonitorBean {
    private String lookuphost;

    public String getLookuphost() {
        return lookuphost;
    }

    public void setLookuphost(String lookuphost) {
        this.lookuphost = lookuphost;
    }
}
