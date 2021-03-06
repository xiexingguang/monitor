package com.ec.monitor.watcher;

import com.ec.monitor.bean.*;
import com.ec.monitor.nsq.NsqUtil;
import com.ec.monitor.properties.Constants;
import com.ec.monitor.properties.NsqwatcherProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Created by xxg on 2015/11/25.
 * nsqWatcher类的职责为，nsq各种节点数据的生成，即远程请求lookup,或者nsqdurl，
 * 获取集群节点状态信息。
 */

public  abstract class NsqWatcher extends  BaseWatcher{

    protected final static Logger LOG = LogManager.getLogger(NsqWatcher.class);
    private static  Map<String/**lookupURL**/, List<NsqProducerBean>> lookupurlNsqprducerMap = null;
    private static Map<String /**lookupurl**/, Map<String/**nsqdURL**/, List<NsqTopicBean>>> nsqdTopicInfoMap = null;
    protected  static Map<String/**topicName**/, NsqTopicConfig> needMonitorTopicMap = null;  //现在只针对一个集群环境配置
    protected String needMonitorTopic; //需要监控的topic地址列表
    protected String lookupurls; // 集群lookup地址，多个用逗号哦隔开
    protected int defaultMsgThreshold; //默认阻塞的消息数量阀值
    protected int defaulttmeoutMsgThreshold; //默认超时消息阀值
    protected int defaultRequeueMsgThreshold; // 默认requeue的消息数量
    protected String isMonitorAlltopic;         // 是否监控所有topic
    protected  long nsqMonitorInteval;       //监控nsq间隔时间
    protected String moduleTopicName;
    protected String moduleNsqdName;
    protected String moduleChannelName;

    @Autowired
    private NsqwatcherProperties nsqwatcherProperties;

    @PostConstruct
    public void start() {

        LOG.info(" =====NSQ 监控 数据初始化开始=====");
        moduleTopicName = Constants.MOUDULE_TOPIC_NAME;
        moduleNsqdName = Constants.MOUDULE_NSQD_NAME;
        moduleChannelName = Constants.MOUDULE_CHANNEL_NAME;
        lookupurls = nsqwatcherProperties.lookupUrl;
        nsqMonitorInteval = nsqwatcherProperties.nsqInteval;
        isMonitorAlltopic = nsqwatcherProperties.monitorAllTopic;
        defaultMsgThreshold = nsqwatcherProperties.defaultTopicThreshold;
        defaulttmeoutMsgThreshold = nsqwatcherProperties.defaultTimeoutThreshold;
        defaultRequeueMsgThreshold = nsqwatcherProperties.defaultRequeThreshold;
        needMonitorTopic = nsqwatcherProperties.needMonitorTopic;

        try {
            needMonitorTopicMap = NsqUtil.msgTopicThresholdMapping(needMonitorTopic, defaultMsgThreshold, defaulttmeoutMsgThreshold, defaultRequeueMsgThreshold);
        } catch (Exception e) {
            if (isMonitorAlltopic.equalsIgnoreCase("yes")) {
                LOG.warn("fail to init the need of the topic config information ");
            } else {
                throw new RuntimeException("fail to init the config of the nsq topic ,the exception is :", e);
            }
        }
       LOG.info("======nsq 监控初始化数据环境完成===config:{\n"+"监控集群地址:"+ lookupurls
                                                                                                  +"\n 监控间隔时间:"+ nsqMonitorInteval +"( millseconds)"
                                                                                                  +"\n是否全量topic监控:"+ isMonitorAlltopic
                                                                                                  +"\n 默认阻塞消息阀值:"+ defaultMsgThreshold
                                                                                                  +"\n 默认超时消息阀值:"+  defaulttmeoutMsgThreshold
                                                                                                  +"\n默认requeue阀值" +  defaultRequeueMsgThreshold+"\n}");
    }

    public  void init() {
        LOG.info(" =======调用远程nsq/ndes,nsqdurl/stats?format=json 接口，生成nsqd各项数据==========");
        lookupurlNsqprducerMap = NsqUtil.getNsqProducersByLOOKupUrls(lookupurls); //有多少个集群环境就发送多少次请求
        nsqdTopicInfoMap = NsqUtil.getClusterTopic(lookupurlNsqprducerMap);
        checkNsqTopicConfig();
    }

    private void checkNsqTopicConfig() {
         //TODO 检测 配置文件topic 是否在集群环境中
    }


    /**
     * 根据集群中的nsqTopicBean 生成对应的NsqChannelMonitor数据
     * 不处理异常，直接抛到上层处理
     * @return
     */
    protected Map<String/**lookupURL**/,List<NsqChannelMonitorBean>> generateNsqChannelMonoitorData() {
        List<String> urls = Arrays.asList(lookupurls.split(","));
        Map<String/**lookupURL**/, List<NsqChannelMonitorBean>> map = new HashMap<>();
        for (String url : urls) {
            Map<String/**nsqdURL**/, List<NsqTopicBean>> topicMap = nsqdTopicInfoMap.get(url);
            List<NsqChannelMonitorBean> nsqChannelMonitorBeans = new ArrayList<>();
            for (String nsqdUrl : topicMap.keySet()) {
                List<NsqTopicBean> topics = topicMap.get(nsqdUrl);
                List<NsqChannelMonitorBean> nsqChannels = NsqUtil.convertNsqtopicBean2NsqchannelMonitorBean(topics, nsqdUrl, url);
                nsqChannelMonitorBeans.addAll(nsqChannels);
            }
            map.put(url, nsqChannelMonitorBeans);
        }
        LOG.info(" success generate the channel monitor data ");
        return map;
    }

    /**
     * 根据集群中的nsqtopic 数据生成nsqMonitorBean数据
     * @return
     */
    protected Map<String/**lookupURL**/,List<NsqdMonitorBean>> generateNsqdMonoitorData() {
        List<String> urls = Arrays.asList(lookupurls.split(","));
        Map<String/**lookupURL**/, List<NsqdMonitorBean>> map = new HashMap<>();
        for (String url : urls) {
            Map<String/**nsqdURL**/, List<NsqTopicBean>> topicMap = nsqdTopicInfoMap.get(url);
            List<NsqdMonitorBean> nsqdMonitorBeans = new ArrayList<>();
            for (String nsqdUrl : topicMap.keySet()) {
                List<NsqTopicBean> topics = topicMap.get(nsqdUrl);
                List<NsqdMonitorBean> nsqMonitor = NsqUtil.convertNsqTopicBean2NsqdMonitorBean(topics, url, nsqdUrl);
                nsqdMonitorBeans.addAll(nsqMonitor);
            }
            map.put(url, nsqdMonitorBeans);
        }
        LOG.info("success generate the nsqd monitor data ");
        return map;

    }

    /**
     * 由原始topicbean 生成topicMonitor监控需要的数据
     * @return
     */
    protected Map<String/**lookupURL**/,List<NsqTopicMonitorBean>> generateNsqTopicData() {
        List<String> urls = Arrays.asList(lookupurls.split(","));
        Map<String/**lookupURL**/, List<NsqTopicMonitorBean>> map = new HashMap<>();
        for (String url : urls) {
            Map<String/**nsqdURL**/, List<NsqTopicBean>> topics = nsqdTopicInfoMap.get(url);
            List<NsqTopicMonitorBean> topicMonitorBeans = NsqUtil.convertTopic2NsqTopicMonitorBean(url, topics);
            map.put(url, topicMonitorBeans);
        }
        LOG.info("success generate the topic  monitor data ");
        return map;
    }

    @Override
    protected void generatedViewData() {
        LOG.info("begin generate  the topic,nsqd,channle  data ,excute by 【NsqWatcher】");
        init();
        generateNsqTopicViewData();
        generateNsqdViewData();
        generateNsqChannelViewData();
    }

    /**
     * 将生成好的monitor数据----》recordView----->入底层map 存储
     */
    protected  abstract void  generateNsqTopicViewData();

    protected   abstract void generateNsqdViewData();

    protected   abstract void generateNsqChannelViewData() ;




    public NsqwatcherProperties getNsqwatcherProperties() {
        return nsqwatcherProperties;
    }

    public  Map<String, Map<String, List<NsqTopicBean>>> getNsqdTopicInfoMap() {
        return nsqdTopicInfoMap;
    }

    public  Map<String, NsqTopicConfig> getNeedMonitorTopicMap() {
        return needMonitorTopicMap;
    }

    public String getNeedMonitorTopic() {
        return needMonitorTopic;
    }

    public String getLookupurls() {
        return lookupurls;
    }

    public int getDefaultMsgThreshold() {
        return defaultMsgThreshold;
    }

    public String getIsMonitorAlltopic() {
        return isMonitorAlltopic;
    }

    public long getNsqMonitorInteval() {
        return nsqMonitorInteval;
    }

    public int getDefaulttmeoutMsgThreshold() {
        return defaulttmeoutMsgThreshold;
    }

    public int getDefaultRequeueMsgThreshold() {
        return defaultRequeueMsgThreshold;
    }

    public String getModuleTopicName() {
        return moduleTopicName;
    }

    public String getModuleNsqdName() {
        return moduleNsqdName;
    }

    public String getModuleChannelName() {
        return moduleChannelName;
    }

    public static Map<String, List<NsqProducerBean>> getLookupurlNsqprducerMap() {
        return lookupurlNsqprducerMap;
    }
}
