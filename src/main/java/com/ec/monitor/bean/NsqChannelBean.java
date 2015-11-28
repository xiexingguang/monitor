package com.ec.monitor.bean;

/**
 * Created by ecuser on 2015/11/26.
 */
public class NsqChannelBean {

    private int depth;
    private int backend_depth;
    private int inflt;
    private int def;
    private int time_out_count;
    private int requeue_count;
    private String channelName;
    private int msg;

    private  int blocknum; /**阻塞消息数量，包括在队列里面的，以及正在消费，但是还木有finished的消息**/

    public int getBlocknum() {
        return depth + inflt;
    }

    public String getChannelName() {
        return channelName;
    }

    public NsqChannelBean setChannelName(String channelName) {
        this.channelName = channelName;
        return this;
    }

    public int getDepth() {
        return depth;
    }

    public NsqChannelBean setDepth(int depth) {
        this.depth = depth;
        return this;
    }

    public int getBackend_depth() {
        return backend_depth;
    }

    public NsqChannelBean setBackend_depth(int backend_depth) {
        this.backend_depth = backend_depth;
        return this;
    }

    public int getTime_out_count() {
        return time_out_count;
    }

    public NsqChannelBean setTime_out_count(int time_out_count) {
        this.time_out_count = time_out_count;
        return this;
    }

    public int getRequeue_count() {
        return requeue_count;
    }

    public NsqChannelBean setRequeue_count(int requeue_count) {
        this.requeue_count = requeue_count;
        return this;
    }

    public int getInflt() {
        return inflt;
    }

    public NsqChannelBean setInflt(int inflt) {
        this.inflt = inflt;
        return this;
    }



    public NsqChannelBean setDef(int def) {
        this.def = def;
        return this;
    }



    public int getMsg() {
        return msg;
    }

    public NsqChannelBean setMsg(int msg) {
        this.msg = msg;
        return this;
    }







}
