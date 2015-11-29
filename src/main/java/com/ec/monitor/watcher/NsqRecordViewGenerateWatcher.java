package com.ec.monitor.watcher;

import com.ec.monitor.bean.NsqChannelMonitorBean;
import com.ec.monitor.bean.NsqTopicConfig;
import com.ec.monitor.bean.NsqTopicMonitorBean;
import com.ec.monitor.bean.NsqdMonitorBean;
import com.ec.monitor.nsq.NsqUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by jasshine_xxg on 2015/11/29.
 */
@Component
public class NsqRecordViewGenerateWatcher extends  NsqWatcher {

    protected Map<String/**topicName**/, NsqTopicConfig> needMonitorTopicMap = getNeedMonitorTopicMap();

    @Override
    protected Map</**lookupURL**/,List<RecordView>> generateNsqTopicViewData(){

    }
    @Override
    protected  Map</**lookupURL**/,List<RecordView>> generateNsqdViewData() {

    }
    @Override
    protected Map</**lookupURL**/,List<RecordView>> generateNsqChannelViewData() {
        Map</**lookupURL**/, List<NsqChannelMonitorBean>> channelMap = generateNsqChannelMonoitorData();
        Map<String/*模块名称or reqort名称*/, Map<String/**需要集中显示的数据**/,List<RecordView>>> recordViewsMap
    }



    private List<RecordView> convertNsqChannelMonoitorData2ReocrdView(List<NsqChannelMonitorBean> nsqChannelMonitorBeans) {
        List<RecordView> recordViews = new ArrayList<RecordView>();
        for (int i = 0; i < nsqChannelMonitorBeans.size(); i++) {
            NsqChannelMonitorBean nsqChannelMonitorBean = nsqChannelMonitorBeans.get(i);
            String lookupurl = nsqChannelMonitorBean.getLookuphost();
            String nsqdHost = nsqChannelMonitorBean.getNsqdhost();
            String topicName = nsqChannelMonitorBean.getTopicName();
            String channelName = nsqChannelMonitorBean.getChannelName();
            boolean lookupisok = nsqChannelMonitorBean.isLookupisOk();
            boolean nsqdisok = nsqChannelMonitorBean.isNsqdisOk();
            int blockNum = nsqChannelMonitorBean.getBlock_num();
            int timeoutNum = nsqChannelMonitorBean.getTimeout_num();
            int requeNum = nsqChannelMonitorBean.getRequeue_num();
            RecordView recordView = NsqUtil.generateNormalVew(lookupurl, new Date(), lookupisok);
            recordView.addItem(new ItemView(nsqdisok?DataType.ItemState.OK:DataType.ItemState.WARNING,nsqdHost));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE,channelName));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE, topicName));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE, blockNum));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE,timeoutNum));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE,requeNum));
            recordViews.add(recordView);
        }
        return recordViews;
    }

    private List<RecordView> convertNsqdMonitorData2RecordView(List<NsqdMonitorBean> nsqdMonitorBeanList) {
        List<RecordView> recordViews = new ArrayList<RecordView>();
        for (int i = 0; i < nsqdMonitorBeanList.size(); i++) {
            NsqdMonitorBean nsqdMonitorBean = nsqdMonitorBeanList.get(i);
            String lookupurl = nsqdMonitorBean.getLookupHost();
            String nsqdhost = nsqdMonitorBean.getNsqdHost();
            String topicName = nsqdMonitorBean.getTopicName();
            boolean nsqdisok = nsqdMonitorBean.isNsqdisOk();
            boolean nsqlookupisok = nsqdMonitorBean.isLookupisOk();
            int blockNum = nsqdMonitorBean.getBlock_num();
            int timeoutNum = nsqdMonitorBean.getTimeout_num();
            int requeueNum = nsqdMonitorBean.getRequeue_num();
            RecordView recordView = NsqUtil.generateNormalVew(lookupurl, new Date(), nsqlookupisok);
            recordView.addItem(new ItemView(nsqdisok?DataType.ItemState.OK:DataType.ItemState.WARNING,nsqdHost));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE,topicName));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE,blockNum));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE,timeoutNum));
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE,requeueNum));
            recordViews.add(recordView);
        }
        return recordViews;
    }

    private List<RecordView> convertNsqTopicMonitorData2RecordView(List<NsqTopicMonitorBean> nsqTopicMonitorBeans) {
        List<RecordView> recordViews = new ArrayList<RecordView>();
        for (int i = 0; i < nsqTopicMonitorBeans.size(); i++) {
            NsqTopicMonitorBean nsqTopicMonitorBean = nsqTopicMonitorBeans.get(i);
            String lookupurl = nsqTopicMonitorBean.getLookuphost();
            String topicName = nsqTopicMonitorBean.getTopicName();
            int blockNum = nsqTopicMonitorBean.getBlock_num();
            int timeoutNum = nsqTopicMonitorBean.getTimeout_num();
            int requeueNum = nsqTopicMonitorBean.getRequeue_num();
            NsqTopicConfig config = needMonitorTopicMap.get(topicName);
            RecordView recordView = NsqUtil.generateNormalVew(lookupurl, new Date(), nsqlookupisok);
            recordView.addItem(new ItemView(DataType.ItemState.IGNORE,topicName));
            recordView.addItem(new ItemView(blockNum>config.getBlockMsgThreshold()?DataType.ItemState.WARING:DataType.ItemState.OK,blockNum));
            recordView.addItem(new ItemView(timeoutNum>config.getTimeoutMsgThreshold()?DataType.ItemState.WARING:DataType.ItemState.OK,timeoutNum));
            recordView.addItem(new ItemView(requeueNum>config.getRequeueMsgThreshold()?DataType.ItemState.WARING:DataType.ItemState.OK,requeueNum));
            recordViews.add(recordView);
        }
        return recordViews;
    }

}
