package com.ec.monitor.bean;

/**
 * Created by ecuser on 2015/11/27.
 */
public class NsqdMonitorBean extends  AbstractNsqMonitorBean {

    private String nsqdHost;
    private String lookupHost;

    public String getNsqdHost() {
        return nsqdHost;
    }

    public NsqdMonitorBean setNsqdHost(String nsqdHost) {
        this.nsqdHost = nsqdHost;
        return this;
    }

    public String getLookupHost() {
        return lookupHost;
    }

    public NsqdMonitorBean setLookupHost(String lookupHost) {
        this.lookupHost = lookupHost;
        return this;
    }
}
