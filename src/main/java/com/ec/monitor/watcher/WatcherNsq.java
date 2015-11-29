package com.ec.monitor.watcher;

import com.ec.watcher.model.DataType;
import com.ec.watcher.model.DataView;
import com.ec.watcher.model.WatcherView;
import com.ec.watcher.task.BaseReportGenerationTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by ecuser on 2015/11/25.
 */
public class WatcherNsq extends BaseReportGenerationTask {

    protected final static Logger LOG = LogManager.getLogger(WatcherNsq.class);

    @Override
    protected String getReportName() {
        return null;
    }

    @Override
    protected long getInterval() {
        return 0;
    }

    @Override
    protected Calendar getStartTime() {
        return null;
    }


    //每隔interval 时间执行该方法
    @Override
    protected WatcherView generateWatcherView() {
        WatcherView view_root = new WatcherView("1.0", "nsq-topic-monitor", new Date(System.currentTimeMillis()), 3600);
        DataView topicDataview = new DataView("nsq-topic", DataType.DiagramStyle.list, DataType.DiagramValue.table);
        topicDataview.addField("监控开始时间");
        topicDataview.addField("lookup-host");
        topicDataview.addField("topic名称");
        topicDataview.addField("阻塞消息数量");
        topicDataview.addField("超时消息数量");
        topicDataview.addField("回退消息数量");

        DataView nsqdNodeView = new DataView("nsq-nsqd", DataType.DiagramStyle.list, DataType.DiagramValue.table);
        nsqdNodeView.addField("监控开始时间");
        nsqdNodeView.addField("lookup-host");
        nsqdNodeView.addField("nsqd-host");
        nsqdNodeView.addField("消息topic");
        nsqdNodeView.addField("阻塞消息数量");
        nsqdNodeView.addField("超时消息数量");
        nsqdNodeView.addField("回退消息数量");

        DataView channelView = new DataView("nsq-channel", DataType.DiagramStyle.list, DataType.DiagramValue.table);
        channelView.addField("监控开始时间");
        channelView.addField("lookup-host");
        channelView.addField("nsqd-host");
        channelView.addField("channel-name");
        channelView.addField("消息topic");
        channelView.addField("阻塞消息数量");
        channelView.addField("超时消息数量");
        channelView.addField("回退消息数量");


        return null;
    }
}
