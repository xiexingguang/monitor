package com.ec.monitor.bean;

/**
 * Created by ecuser on 2015/11/27.
 */
public class NsqChannelMonitorBean extends AbstractNsqMonitorBean {


    private String channelName;
    private String lookuphost;
    private String nsqdhost;
    private boolean lookupisOk;
    private boolean nsqdisOk;

    public boolean isNsqdisOk() {
        return nsqdisOk;
    }

    public NsqChannelMonitorBean setNsqdisOk(boolean nsqdisOk) {
        this.nsqdisOk = nsqdisOk;
        return this;
    }

    public boolean isLookupisOk() {
        return lookupisOk;
    }

    public NsqChannelMonitorBean setLookupisOk(boolean lookupisOk) {
        this.lookupisOk = lookupisOk;
        return this;
    }




    public String getNsqdhost() {
        return nsqdhost;
    }

    public NsqChannelMonitorBean setNsqdhost(String nsqdhost) {
        this.nsqdhost = nsqdhost;
        return this;
    }

    public String getLookuphost() {
        return lookuphost;
    }

    public NsqChannelMonitorBean setLookuphost(String lookuphost) {
        this.lookuphost = lookuphost;
        return this;
    }


    public String getChannelName() {
        return channelName;
    }

    public NsqChannelMonitorBean setChannelName(String channelName) {
        this.channelName = channelName;
        return this;
    }
}
