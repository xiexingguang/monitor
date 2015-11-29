package com.ec.monitor.watcher;

import com.ec.monitor.bean.*;
import com.ec.monitor.nsq.NsqUtil;
import com.ec.monitor.properties.NsqwatcherProperties;
import com.ec.monitor.util.StringUtil;
import com.ec.watcher.model.RecordView;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import org.apache.cxf.transport.TransportURIResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by xxg on 2015/11/25.
 * nsqWatcher类的职责为，nsq各种节点数据的生成，即远程请求lookup,或者nsqdurl，
 * 获取集群节点状态信息。
 */
@Component
public abstract class NsqWatcher extends  BaseWatcher{

    private static  Map<String/**lookupURL**/, List<NsqProducerBean>> lookupurlNsqprducerMap = null;
    private static Map<String/**nsqdURL**/, List<NsqTopicBean>> nsqdTopicInfoMap = null;

    protected static Map<String/**topicName**/, NsqTopicConfig> needMonitorTopicMap = null;
    protected String needMonitorTopic; //需要监控的topic地址列表
    protected String lookupurls; // 集群lookup地址，多个用逗号哦隔开
    protected int defaultMsgThreshold; //默认阻塞的消息数量阀值
    protected String isMonitorAlltopic; // 是否监控所有topic
    protected  long nsqMonitorInteval; //监控nsq间隔时间
    protected int defaulttmeoutMsgThreshold;
    protected int defaultRequeueMsgThreshold;

    public NsqwatcherProperties getNsqwatcherProperties() {
        return nsqwatcherProperties;
    }

    public long getNsqMonitorInteval() {
        return nsqMonitorInteval;
    }

    public int getDefaultMsgThreshold() {
        return defaultMsgThreshold;
    }

    public String getLookupurls() {
        return lookupurls;
    }

    public String getNeedMonitorTopic() {
        return needMonitorTopic;
    }

    public static Map<String, List<NsqProducerBean>> getLookupurlNsqprducerMap() {
        return lookupurlNsqprducerMap;
    }

    public static Map<String, List<NsqTopicBean>> getNsqdTopicInfoMap() {
        return nsqdTopicInfoMap;
    }

    public String getIsMonitorAlltopic() {
        return isMonitorAlltopic;
    }

    @Autowired
    private NsqwatcherProperties nsqwatcherProperties;

    public NsqWatcher() {
        nsqMonitorInteval = nsqwatcherProperties.nsqInteval;
        isMonitorAlltopic = nsqwatcherProperties.monitorAllTopic;
        defaultMsgThreshold = nsqwatcherProperties.defaultTopicThreshold;
        defaulttmeoutMsgThreshold = nsqwatcherProperties.defaultTimeoutThreshold;
        defaultRequeueMsgThreshold = nsqwatcherProperties.defaultRequeThreshold;
        needMonitorTopic = nsqwatcherProperties.needMonitorTopic;
        needMonitorTopicMap = NsqUtil.msgTopicThresholdMapping(needMonitorTopic,defaultMsgThreshold,defaulttmeoutMsgThreshold,defaultRequeueMsgThreshold);
        }

    }

    public static Map<String, NsqTopicConfig> getNeedMonitorTopicMap() {
        return needMonitorTopicMap;
    }


    public  void init() {
        lookupurlNsqprducerMap = NsqUtil.getNsqProducersByLOOKupUrls(lookupurls); //发送一次lookupurl请求
        nsqdTopicInfoMap = NsqUtil.generateNsqdNodesByUrls(lookupurls); //有多少个nsqd节点就发送多少次请求
    }

    protected Map</**lookupURL**/,List<NsqChannelMonitorBean>> generateNsqChannelMonoitorData() {


    }

    protected Map</**lookupURL**/,List<NsqdMonitorBean>> generateNsqdMonoitorData() {


    }

    protected Map</**lookupURL**/,List<NsqTopicMonitorBean>> generateNsqTopicData() {


    }

    @Override
    protected void generatedViewData() {
        init();
        generateNsqTopicViewData();
        generateNsqdViewData();
        generateNsqChannelViewData();

    }


    protected abstract  Map</**lookupURL**/,List<RecordView>> generateNsqTopicViewData();
    protected abstract  Map</**lookupURL**/,List<RecordView>> generateNsqdViewData();
    protected abstract  Map</**lookupURL**/,List<RecordView>> generateNsqChannelViewData();



    private List<RecordView> generateTopicData() {
        //生成以topic为维度的数据，即topic
        List<NsqTopicMonitorBean> nsqTopicMonitorBeanList = new ArrayList<NsqTopicMonitorBean>();
       // NsqTopicMonitorBean monitorBean = new
    }

    private List<RecordView> generateChannelData(String lookupUrl) {

        List<RecordView> recordViews = new ArrayList<RecordView>();
        List<NsqChannelMonitorBean> monitorBeans = null;






        boolean lookupisOK = true;


        List<NsqProducerBean> nsqProducerBeans = NsqUtil.getNsqproducers(lookupUrl);
        if (nsqProducerBeans == null) {
            lookupisOK = false;
        }
        for (int i = 0; i < nsqProducerBeans.size(); i++) {
            String nsqdurl = nsqProducerBeans.get(i).getGetNodesStatsUrl();
            List<NsqTopicBean> nsqdTopics = NsqUtil.generateNsqdNodeInfoByNsqdUrl(nsqdurl);
            if (nsqdTopics == null) {  //说明nsqd连接发生异常，

            }

        }

        for (int i = 0; i < monitorBeans.size(); i++) {
            RecordView recordView = NsqUtil.generateNormalVew(lookupurl, lookupisOK);
            recordView.addItem();

        }



    }

    private List<RecordView> generateNsqdData() {
        //TODO1.获取所有nsqd 的topic状态，算法统计结果
        final String lookurls = nsqwatcherProperties.lookupUrl;
        List<String> lookupLists = Arrays.asList(lookurls.split(","));
        List<String> unconnectedNsqd = new ArrayList<String>();
        //一个nsq集群环境，对应一个lookupurl 即可
        Map<String/**LOOKUP URL**/, List<NsqProducerBean>> producersMap = NsqUtil.getNsqProducersByLOOKupUrls(lookupLists);
        for (Map.Entry<String, List<NsqProducerBean>> entry : producersMap.entrySet()) {
            for(NsqProducerBean nsqProducerBean :entry.getValue()) {
                String nsqURL = nsqProducerBean.getGetNodesStatsUrl();
                List<NsqTopicBean> nsqTopicBeans = NsqUtil.generateNsqdNodeInfoByNsqdUrl(nsqURL);
                if (nsqTopicBeans == null) {
                    unconnectedNsqd.add(nsqURL);
                }
                for (int i = 0; i < nsqTopicBeans.size(); i++) {
                  //  NsqdMonitorBean monitorBean = new NsqdMonitorBean();
                   // monitorBean.setLookupHost()
                 /*   NsqTopicBean nsqTopicBean = nsqTopicBeans.get(i);
                    monitorBean.setNsqdHost(nsqURL);
                    monitorBean.setRequeue_num(nsqTopicBean.get)*/
                    NsqTopicBean nsqTopicBean = nsqTopicBeans.get(i);
                    List<NsqChannelBean> channelBeans = nsqTopicBean.getChannelBeans();
                    if (channelBeans == null) {
                        NsqdMonitorBean monitorBean = new NsqdMonitorBean();
                        monitorBean.setBlock_num(nsqTopicBean.getDepth());
                    }else{
                        for (int j = 0; j < channelBeans.size(); i++) {
                            NsqdMonitorBean monitorBean = new NsqdMonitorBean();
                            monitorBean.setBlock_num(channelBeans.get(i).getBlocknum());

                        }
                    }


                }
            }
        }

        if (producersMap != null) {
            for (String key : producersMap.keySet()) {

            }
        }



        List<NsqdMonitorBean> nsqdMonitorBeans = new ArrayList<NsqdMonitorBean>();

    }


    private List<NsqTopicMonitorBean> generateTopicMonitor() {
        //TODO1.获取



    }





    private  Map<String,List<NsqdMonitorBean>> generateNsqNodeMonitor( Map<String, List<NsqChannelMonitorBean>> map) {
        List<NsqProducerBean> nsqProducerBeans = null;
        List<NsqdMonitorBean> nsqdMonitorBeans = null;
        Map<String, List<NsqdMonitorBean>> nsqmonitmap = null;
        if (nsqProducerBeans == null) {
            NsqdMonitorBean nsqdMonitorBean = new NsqdMonitorBean();
            nsqdMonitorBean.setLookupisOk(false);
            nsqdMonitorBean.setLookupHost("");
            nsqdMonitorBeans.add(nsqdMonitorBean);
            nsqmonitmap.put("lookupurl", nsqdMonitorBeans);

            return nsqmonitmap;
        }

        for (int i = 0; i < nsqProducerBeans.size(); i++) {
            String nsqdurl = nsqProducerBeans.get(i).getGetNodesStatsUrl();
            List<NsqChannelMonitorBean> nsqChannelMonitorBeanList = map.get(nsqdurl);//nsqchannelMonitor不可能为null
            List<NsqdMonitorBean> nsqdMonitorBeans1 = new ArrayList<NsqdMonitorBean>();
            for (int j = 0; j < nsqChannelMonitorBeanList.size(); j++) {
                NsqdMonitorBean nsqdMonitorBean = NsqUtil.convertNsqChannelMontorBean2NsqdMonitorBean()
            }

        }





    }


    private Map<String, List<NsqChannelMonitorBean>> generateChnnelMonitorByLookupURL(String lookupurl) {
        List<NsqProducerBean> nsqProducerBeans = NsqUtil.getNsqproducers(lookupurl);
        Map<String, List<NsqChannelMonitorBean>> nsqchannelMonitorMap = new HashMap<String, List<NsqChannelMonitorBean>>();
        if (nsqProducerBeans == null) {
            List<NsqChannelMonitorBean> nsqChannelMonitorBeans = new ArrayList<NsqChannelMonitorBean>();
            NsqChannelMonitorBean nsqChannelMonitorBean = new NsqChannelMonitorBean();
            nsqChannelMonitorBean.setLookupisOk(false);
            nsqChannelMonitorBean.setLookuphost(lookupurl);
            nsqChannelMonitorBean.setNsqdhost("");
            nsqChannelMonitorBeans.add(nsqChannelMonitorBean);
            nsqchannelMonitorMap.put(lookupurl, nsqChannelMonitorBeans);
            return nsqchannelMonitorMap;
        }
        for (int i = 0; i < nsqProducerBeans.size(); i++) {
            String nsqdUrl = nsqProducerBeans.get(i).getGetNodesStatsUrl();
            List<NsqChannelMonitorBean> channelMonitorBeans = generateChannelMonitorBynsqdURL(nsqdUrl, lookupurl);
            nsqchannelMonitorMap.put(nsqdUrl, channelMonitorBeans);
        }
        return nsqchannelMonitorMap;

    }


    private List<NsqChannelMonitorBean> generateChannelMonitorBynsqdURL(String NSQDURL,String lookupurl) {
        List<NsqTopicBean> topicBeans = NsqUtil.generateNsqdNodeInfoByNsqdUrl(NSQDURL);
        List<NsqChannelMonitorBean> nsqChannelMonitorBeans = new ArrayList<NsqChannelMonitorBean>();
        if (topicBeans == null) {
            NsqChannelMonitorBean nsqChannelMonitorBean = new NsqChannelMonitorBean();
            nsqChannelMonitorBean.setLookuphost(lookupurl);
            nsqChannelMonitorBean.setNsqdhost(NSQDURL);
            nsqChannelMonitorBean.setLookupisOk(true);
            nsqChannelMonitorBean.setNsqdisOk(false);
            nsqChannelMonitorBeans.add(nsqChannelMonitorBean);
            return nsqChannelMonitorBeans;
        }
        for (int i = 0; i < topicBeans.size(); i++) {
            NsqChannelMonitorBean nsqChannelMonitorBean = NsqUtil.convertNsqtopicBean2NsqchannelMonitorBean(topicBeans.get(i));
            nsqChannelMonitorBeans.add(nsqChannelMonitorBean);
        }
        return nsqChannelMonitorBeans;
    }


}
