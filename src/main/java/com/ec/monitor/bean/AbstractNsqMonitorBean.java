package com.ec.monitor.bean;

/**
 * Created by ecuser on 2015/11/27.
 */
public  class AbstractNsqMonitorBean {

    private int timeout_num;
    private int requeue_num;
    private int block_num;
    private String monitorTime;
    private String topicName;
    private boolean lookupisOk;

    public boolean isNsqdisOk() {
        return nsqdisOk;
    }

    public AbstractNsqMonitorBean setNsqdisOk(boolean nsqdisOk) {
        this.nsqdisOk = nsqdisOk;
        return this;
    }

    public boolean isLookupisOk() {
        return lookupisOk;
    }

    public AbstractNsqMonitorBean setLookupisOk(boolean lookupisOk) {
        this.lookupisOk = lookupisOk;
        return this;
    }

    private boolean nsqdisOk;


    public String getTopicName() {
        return topicName;
    }

    public AbstractNsqMonitorBean setTopicName(String topicName) {
        this.topicName = topicName;
        return this;
    }

    public int getTimeout_num() {
        return timeout_num;
    }

    public AbstractNsqMonitorBean setTimeout_num(int timeout_num) {
        this.timeout_num = timeout_num;
        return this;
    }

    public String getMonitorTime() {
        return monitorTime;
    }

    public AbstractNsqMonitorBean setMonitorTime(String monitorTime) {
        this.monitorTime = monitorTime;
        return this;
    }

    public int getBlock_num() {
        return block_num;
    }

    public AbstractNsqMonitorBean setBlock_num(int block_num) {
        this.block_num = block_num;
        return this;
    }

    public int getRequeue_num() {
        return requeue_num;
    }

    public AbstractNsqMonitorBean setRequeue_num(int requeue_num) {
        this.requeue_num = requeue_num;
        return this;
    }
}
