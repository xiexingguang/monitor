package com.ec.monitor.watcher;
import com.ec.monitor.bean.NsqChannelMonitorBean;
import com.ec.monitor.bean.NsqTopicConfig;
import com.ec.monitor.bean.NsqTopicMonitorBean;
import com.ec.monitor.bean.NsqdMonitorBean;
import com.ec.monitor.nsq.NsqUtil;
import com.ec.watcher.model.DataType;
import com.ec.watcher.model.ItemView;
import com.ec.watcher.model.RecordView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by jasshine_xxg on 2015/11/29.
 */
@Component
public class NsqRecordViewGenerateWatcher extends  NsqWatcher {

    protected final static Logger LOG = LogManager.getLogger(NsqUtil.class);
    protected Map<String/**topicName**/, NsqTopicConfig> needMonitorTopicMap = getNeedMonitorTopicMap();

    /**
     * 生成topic模块显示view数据
     */
    @Override
    protected  void generateNsqTopicViewData() {
        try {
            Map<String /**lookupURL**/, List<NsqTopicMonitorBean>> channelMap = generateNsqTopicData();
            Map<String, List<RecordView>> map1 = new HashMap<String, List<RecordView>>();
            for (String key : channelMap.keySet()) {
                String lookupurl = key;  //针对每一个集群环境
                List<NsqTopicMonitorBean> nsqTopicMonitorBeans = channelMap.get(lookupurl);
                List<RecordView> topicViews = convertNsqTopicMonitorData2RecordView(nsqTopicMonitorBeans);
                map1.put(lookupurl, topicViews);
            }
            addView(moduleTopicName, map1);
            LOG.info("success generate the topic  view data.");
        } catch (Exception e) {
            LOG.error("fail to generate the topic view data ,the exception : ", e);
        }
    }
    @Override
    protected  void generateNsqdViewData() {
        try {
            Map<String /**lookupURL**/, List<NsqdMonitorBean>> channelMap = generateNsqdMonoitorData();
            Map<String, List<RecordView>> map1 = new HashMap<String, List<RecordView>>();
            for (String key : channelMap.keySet()) {
                String lookupurl = key;  //针对每一个集群环境
                List<NsqdMonitorBean> nsqdMonitorBeans = channelMap.get(lookupurl);
                List<RecordView> nsqdViews = convertNsqdMonitorData2RecordView(nsqdMonitorBeans);
                map1.put(lookupurl, nsqdViews);
            }
            addView(moduleNsqdName, map1);
            LOG.info("success generate the nsqd view  data.");
        } catch (Exception e) {
            LOG.error("fail to generate the nsqd view  ,the exception :",e);
        }
    }
    @Override
    protected void  generateNsqChannelViewData() {
        try {
            Map<String /**lookupURL**/, List<NsqChannelMonitorBean>> channelMap = generateNsqChannelMonoitorData();
            Map<String, List<RecordView>> map1 = new HashMap<String, List<RecordView>>();
            for (String key : channelMap.keySet()) {
                String lookupurl = key;  //针对每一个集群环境
                List<NsqChannelMonitorBean> nsqChannelMonitorBeans = channelMap.get(lookupurl);
                List<RecordView> channelRecordViews = convertNsqChannelMonoitorData2ReocrdView(nsqChannelMonitorBeans);
                map1.put(lookupurl, channelRecordViews);
            }
            addView(moduleChannelName, map1);
            LOG.info("success generate the channel  data.");
        } catch (Exception e) {
          //  e.printStackTrace();
            LOG.error("fail to generate the channel  data ,the exception : ",e);
        }
    }


    /**针对一个集群环境
     * 将NsqChannelMonitorBean ---> recordView数据方便在图表上显示
     * @param nsqChannelMonitorBeans
     * @return
     */
    private List<RecordView> convertNsqChannelMonoitorData2ReocrdView(List<NsqChannelMonitorBean> nsqChannelMonitorBeans) {
        Map<String/**topicName**/, NsqTopicConfig> needMonitorTopicMap = getNeedMonitorTopicMap();
        List<RecordView> recordViews = new ArrayList<RecordView>();
        for (int i = 0; i < nsqChannelMonitorBeans.size(); i++) {
            NsqChannelMonitorBean nsqChannelMonitorBean = nsqChannelMonitorBeans.get(i);
            String lookupurl = nsqChannelMonitorBean.getLookuphost();
            String nsqdHost = nsqChannelMonitorBean.getNsqdhost();
            String topicName = nsqChannelMonitorBean.getTopicName();
            NsqTopicConfig config = null;

            // 检测topic 是否需要监控
            if (isMonitorAlltopic.equalsIgnoreCase("yes") || needMonitorTopicMap == null) { // 监控集群环境中
                config = new NsqTopicConfig();
                config.setBlockMsgThreshold(defaultMsgThreshold);
                config.setRequeueMsgThreshold(defaultRequeueMsgThreshold);
                config.setTimeoutMsgThreshold(defaulttmeoutMsgThreshold);
            }else if (isMonitorAlltopic.equalsIgnoreCase("no")) {  // 通过配置决定监控哪些topic
                config = needMonitorTopicMap.get(topicName);
                if (config == null) { //说明topic 该topic 不在监控列表里面，则connitue
                    LOG.info("该 topicName　【"+topicName+"】不需要监控");
                    continue;
                }
            }

            String channelName = nsqChannelMonitorBean.getChannelName();
            boolean lookupisok = nsqChannelMonitorBean.isLookupisOk();
            boolean nsqdisok = nsqChannelMonitorBean.isNsqdisOk();
            int blockNum = nsqChannelMonitorBean.getBlock_num();
            int timeoutNum = nsqChannelMonitorBean.getTimeout_num();
            int requeNum = nsqChannelMonitorBean.getRequeue_num();
            RecordView recordView = NsqUtil.generateNormalVew(lookupurl, new Date(), lookupisok);
            recordView.addItem(new ItemView(nsqdisok? DataType.ItemState.OK:DataType.ItemState.WARNING,nsqdHost));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE,channelName));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE, topicName));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE, blockNum+""));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE,timeoutNum+""));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE,requeNum+""));
            recordViews.add(recordView);
        }
        return recordViews;
    }

    /**
     * 将NsqChannelMonitorBean ---> recordView数据方便在图表上显示
     * @param nsqChannelMonitorBeans
     * @return
     */
    private List<RecordView> convertNsqdMonitorData2RecordView(List<NsqdMonitorBean> nsqdMonitorBeanList) {
        Map<String/**topicName**/, NsqTopicConfig> needMonitorTopicMap = getNeedMonitorTopicMap();
        List<RecordView> recordViews = new ArrayList<RecordView>();
        for (int i = 0; i < nsqdMonitorBeanList.size(); i++) {
            NsqdMonitorBean nsqdMonitorBean = nsqdMonitorBeanList.get(i);
            String lookupurl = nsqdMonitorBean.getLookupHost();
            String nsqdhost = nsqdMonitorBean.getNsqdHost();
            String topicName = nsqdMonitorBean.getTopicName();
            NsqTopicConfig config = null;

            // 检测topic 是否需要监控
            if (isMonitorAlltopic.equalsIgnoreCase("yes") || needMonitorTopicMap == null) { // 监控集群环境中
                config = new NsqTopicConfig();
                config.setBlockMsgThreshold(defaultMsgThreshold);
                config.setRequeueMsgThreshold(defaultRequeueMsgThreshold);
                config.setTimeoutMsgThreshold(defaulttmeoutMsgThreshold);
            }else if (isMonitorAlltopic.equalsIgnoreCase("no")) {  // 通过配置决定监控哪些topic
                config = needMonitorTopicMap.get(topicName);
                if (config == null) { //说明topic 该topic 不在监控列表里面，则connitue
                    LOG.info("该 topicName　【"+topicName+"】不需要监控");
                    continue;
                }
            }

            boolean nsqdisok = nsqdMonitorBean.isNsqdisOk();
            boolean nsqlookupisok = nsqdMonitorBean.isLookupisOk();
            int blockNum = nsqdMonitorBean.getBlock_num();
            int timeoutNum = nsqdMonitorBean.getTimeout_num();
            int requeueNum = nsqdMonitorBean.getRequeue_num();
            RecordView recordView = NsqUtil.generateNormalVew(lookupurl, new Date(), nsqlookupisok);
            recordView.addItem(new ItemView(nsqdisok?DataType.ItemState.OK:DataType.ItemState.WARNING,nsqdhost));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE,topicName));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE,blockNum+""));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE,timeoutNum+""));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE,requeueNum+""));
            recordViews.add(recordView);
        }
        return recordViews;
    }

    /**
     * 将NsqChannelMonitorBean ---> recordView数据方便在图表上显示
     * @param nsqChannelMonitorBeans
     * @return
     */
    private List<RecordView> convertNsqTopicMonitorData2RecordView(List<NsqTopicMonitorBean> nsqTopicMonitorBeans) {
         Map<String/**topicName**/, NsqTopicConfig> needMonitorTopicMap = getNeedMonitorTopicMap();
        List<RecordView> recordViews = new ArrayList<RecordView>();
        for (int i = 0; i < nsqTopicMonitorBeans.size(); i++) {
            NsqTopicMonitorBean nsqTopicMonitorBean = nsqTopicMonitorBeans.get(i);
            String lookupurl = nsqTopicMonitorBean.getLookuphost();
            boolean nsqlookupisok = nsqTopicMonitorBean.isLookupisOk();
            String topicName = nsqTopicMonitorBean.getTopicName();
            NsqTopicConfig config = null;

            // 检测topic 是否需要监控
            if (isMonitorAlltopic.equalsIgnoreCase("yes") || needMonitorTopicMap == null) { // 监控集群环境中
                config = new NsqTopicConfig();
                config.setBlockMsgThreshold(defaultMsgThreshold);
                config.setRequeueMsgThreshold(defaultRequeueMsgThreshold);
                config.setTimeoutMsgThreshold(defaulttmeoutMsgThreshold);
            }else if (isMonitorAlltopic.equalsIgnoreCase("no")) {  // 通过配置决定监控哪些topic
                config = needMonitorTopicMap.get(topicName);
                if (config == null) { //说明topic 该topic 不在监控列表里面，则connitue
                    LOG.info("该 topicName　【"+topicName+"】不需要监控");
                    continue;
                }
            }

            int blockNum = nsqTopicMonitorBean.getBlock_num();
            int timeoutNum = nsqTopicMonitorBean.getTimeout_num();
            int requeueNum = nsqTopicMonitorBean.getRequeue_num();

            RecordView recordView = NsqUtil.generateNormalVew(lookupurl, new Date(), nsqlookupisok);
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE,topicName));
            recordView.addItem(new ItemView(blockNum>config.getBlockMsgThreshold()?DataType.ItemState.WARNING:DataType.ItemState.OK,blockNum+""));
            recordView.addItem(new ItemView(timeoutNum>config.getTimeoutMsgThreshold()?DataType.ItemState.WARNING:DataType.ItemState.OK,timeoutNum+""));
            recordView.addItem(new ItemView(requeueNum>config.getRequeueMsgThreshold()?DataType.ItemState.WARNING:DataType.ItemState.OK,requeueNum+""));
            recordViews.add(recordView);
        }
        return recordViews;
    }

}
