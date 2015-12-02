package com.ec.monitor.watcher;

import com.ec.watcher.model.DataType;
import com.ec.watcher.model.DataView;
import com.ec.watcher.model.RecordView;
import com.ec.watcher.model.WatcherView;
import com.ec.watcher.task.BaseReportGenerationTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by ecuser on 2015/11/25.
 */
@Component
public class WatcherNsq extends BaseReportGenerationTask {

    protected final static Logger LOG = LogManager.getLogger(WatcherNsq.class);

    @Override
    protected String getReportName() {
        return "nsq-monitor";
    }

    @Override
    protected long getInterval() {
        return nsqRecordViewGenerateWatcher.nsqMonitorInteval;
    }

    @Override
    protected Calendar getStartTime() {
        return null;
    }

    @Autowired
    private NsqRecordViewGenerateWatcher nsqRecordViewGenerateWatcher;


    //每隔interval 时间执行该方法
    @Override
    protected WatcherView generateWatcherView() {
        LOG.info("执行generateWatcherView 方法开始,执行时间为:【"+new Date().toLocaleString()+"】");
        nsqRecordViewGenerateWatcher.generatedViewData(); //生成nsqd 数据
        Map<String/**模块名称or reqort名称**/, Map<String/**需要集中显示的数据**/, List<RecordView>>> recordViewsMap = nsqRecordViewGenerateWatcher.getRecordViewsMap();
        String lookupurls = nsqRecordViewGenerateWatcher.lookupurls;
        List<String> urls = Arrays.asList(lookupurls.split(","));

        WatcherView view_root = new WatcherView("1.0", "nsq-topic-monitor", new Date(System.currentTimeMillis()), 3600);

        try {
            DataView topicDataview = new DataView("nsq-topic", DataType.DiagramStyle.list, DataType.DiagramValue.table);
            topicDataview.addField("监控开始时间");
            topicDataview.addField("lookup-host");
            topicDataview.addField("topic名称");
            topicDataview.addField("阻塞消息数量");
            topicDataview.addField("超时消息数量");
            topicDataview.addField("回退消息数量");

            Map<String/**需要集中显示的数据,lookupurl**/, List<RecordView>>
                    topic_records = recordViewsMap.get(nsqRecordViewGenerateWatcher.moduleTopicName);

            if (topic_records == null) {
                LOG.warn("nsqd dataview  is null,please check.. ");
            }

            for (String lookupurl : urls) {
                if(topic_records !=null) {
                    List<RecordView> recordViews = topic_records.get(lookupurl);
                    for (RecordView recordView : recordViews) {
                        topicDataview.addRecord(recordView);
                    }
                }
            }
            view_root.addData(topicDataview);
            LOG.info("success generate dataview of the topic");
        } catch (Throwable e) { //防御性容错
             LOG.error("fail to generate the  topic dataview  data,the exception is:", e);
        }

        try {
            DataView nsqdNodeView = new DataView("nsq-nsqd", DataType.DiagramStyle.list, DataType.DiagramValue.table);
            nsqdNodeView.addField("监控开始时间");
            nsqdNodeView.addField("lookup-host");
            nsqdNodeView.addField("nsqd-host");
            nsqdNodeView.addField("消息topic");
            nsqdNodeView.addField("阻塞消息数量");
            nsqdNodeView.addField("超时消息数量");
            nsqdNodeView.addField("回退消息数量");

            Map<String/**需要集中显示的数据,lookupurl**/, List<RecordView>>
                    nsqd_topic = recordViewsMap.get(nsqRecordViewGenerateWatcher.moduleNsqdName);
            if (nsqd_topic == null) {
                LOG.warn("nsqd dataview  is null,please check.. ");
            }

            for (String lookupurl : urls) {
                if(nsqd_topic != null){
                    List<RecordView> recordViews = nsqd_topic.get(lookupurl);
                    for (RecordView recordView : recordViews) {
                        nsqdNodeView.addRecord(recordView);
                    }
                }
            }
            view_root.addData(nsqdNodeView);
            LOG.info("success generate dataview of the nsqd");
        } catch (Throwable e) {
            LOG.error("fail to generate the  nsqd dataview data,the exception is:",e);
        }


        try {
            DataView channelView = new DataView("nsq-channel", DataType.DiagramStyle.list, DataType.DiagramValue.table);
            channelView.addField("监控开始时间");
            channelView.addField("lookup-host");
            channelView.addField("nsqd-host");
            channelView.addField("消息topic");
            channelView.addField("channel-name");
            channelView.addField("阻塞消息数量");
            channelView.addField("超时消息数量");
            channelView.addField("回退消息数量");

            Map<String/**需要集中显示的数据,lookupurl**/, List<RecordView>>
                    channel_topic = recordViewsMap.get(nsqRecordViewGenerateWatcher.moduleChannelName);

            if (channel_topic == null) {
                LOG.warn("channel dataview  is null,please check.. ");
            }

            for (String lookupurl : urls) {
                if(channel_topic != null) {
                    List<RecordView> recordViews = channel_topic.get(lookupurl);
                    for (RecordView recordView : recordViews) {
                        channelView.addRecord(recordView);
                    }
                }
            }
            view_root.addData(channelView);
            LOG.info("success generate dataview of the channel");
        } catch (Throwable e) {
            LOG.error("fail to generate the  channel dataview  data,the exception is:", e);
        }

        return view_root;
    }
}
