package com.ec.monitor.nsq;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ec.monitor.bean.*;
import com.ec.monitor.properties.NsqwatcherProperties;
import com.ec.monitor.util.StringUtil;
import com.ec.monitor.util.UrlConnectionUtil;
import com.ec.watcher.model.DataType;
import com.ec.watcher.model.ItemView;
import com.ec.watcher.model.RecordView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by xxg on 2015/11/26.
 */
@Component
public class NsqUtil {

    protected final static Logger LOG = LogManager.getLogger(NsqUtil.class);

    @Autowired
    private NsqwatcherProperties nsqwatcherProperties;

    /**
     * 根据lookup地址获取 nsqd 生产者地址
     * @param url
     * @return if return null ,means the nsqlookup has been down
     */
    public static List<NsqProducerBean> getNsqproducers(String url) {
        LOG.info("获取lookup 所有nsqd 节点信息开始====》请求url为:"+url);
        if (!url.startsWith("http://")) {
            url = "http://" + url;
        }
        url = url + "/nodes";
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        List<NsqProducerBean> nsqProducerBeans = null;
        try {
            connection = UrlConnectionUtil.openGetConnection(url);
            if (connection.getContentType().contains("application/json")) {
                inputStream = connection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                String returnJson = null;
                while ((returnJson = bufferedReader.readLine()) != null){
                    JSONObject json = JSON.parseObject(returnJson, JSONObject.class);
                    Integer status = json.getInteger("status_code");
                    String isOk = json.getString("status_txt");
                    if (status == 200 && isOk.equalsIgnoreCase("OK")) {
                        nsqProducerBeans = new ArrayList<NsqProducerBean>();  //实例化
                        JSONObject datajson = json.getJSONObject("data");
                        JSONArray producers = (JSONArray) datajson.getJSONArray("producers");
                        for (int i = 0; producers != null && i < producers.size(); i++) {
                            NsqProducerBean nsqProducerBean = new NsqProducerBean();
                            JSONObject producer = producers.getJSONObject(i);
                            String hosturl = producer.getString("broadcast_address");
                            int port = producer.getInteger("http_port");
                            nsqProducerBean.setUrl(hosturl);
                            nsqProducerBean.setPort(port);
                            List<NsqTopicBean> topicBeans = new ArrayList<NsqTopicBean>();
                            JSONArray topics = producer.getJSONArray("topics");
                            for (int j = 0; topics != null && j < topics.size(); j++) {
                                String topicName = topics.getString(j);
                                NsqTopicBean topicBean = new NsqTopicBean();
                                topicBean.setTopicName(topicName);
                                topicBeans.add(topicBean);
                            }
                            nsqProducerBean.setNsqTopicBeans(topicBeans);
                            nsqProducerBeans.add(nsqProducerBean);
                        }//end for
                    }//end if

                }//end while
            }
        } catch (IOException e) { //不处理
            LOG.warn("根据lookup获取nsqd节点信息异常，异常lookupurl地址为"+url,e);
        }finally {
           /* try {
                inputStream.close(); //直接自己关闭了，如果在调用报inputStream为null指针
                bufferedReader.close();
            } catch (IOException e) {
                inputStream = null;
                bufferedReader = null;
            }*/
        }
        LOG.info("获取nsqd节点信息完成，所有nsqd 信息为===》" + JSONObject.toJSONString(nsqProducerBeans));
        return nsqProducerBeans;
    }

    /**
     * 批量根据lookupurls　地址获取每个集群的nsqd地址
     * @param lookupurls ，地址形式为：142.14.13.4:9000,192.144.13.41:2000
     * @return
     */
    public static Map<String/**LOOKUP URL**/, List<NsqProducerBean>> getNsqProducersByLOOKupUrls(String  lookupurls) {
        if (StringUtil.isNullString(lookupurls)) {
            throw new IllegalArgumentException("错误的lookupurls 地址" + lookupurls);
        }
        List<String> urls = Arrays.asList(lookupurls.split(","));
        Map<String/**LOOKUP URL**/, List<NsqProducerBean>> nsqproduces = new HashMap<String/**LOOKUP URL**/, List<NsqProducerBean>>();
        for (int i = 0; urls != null && i < urls.size(); i++) {
            String url = urls.get(i);
            List<NsqProducerBean> nsqProducerBeans = getNsqproducers(url);
            nsqproduces.put(url, nsqProducerBeans);
        }
        return nsqproduces;
    }



    /**
     * 根据nsqd 获取 nsqd 下具体的topic 信息
     * @param url : nsqd地址如：10.0.200.51:7006
     * @return
     */
    public static List<NsqTopicBean> generateNsqdNodeInfoByNsqdUrl(String url) {
         LOG.info("获取nsqd节点topic 相关信息，获取nsqd节点url为============》"+url);
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;

        if (!url.startsWith("http://")) {
            url = "http://" + url;
        }
        String requestUrl = url + "/stats?format=json";
        List<NsqTopicBean> nsqTopicBeanList = null;

        try {
             connection = UrlConnectionUtil.openGetConnection(requestUrl);
            if (connection.getContentType().contains("application/json")) {
                inputStream = connection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                String returnJson = null;
                while ((returnJson = bufferedReader.readLine()) != null){
                    JSONObject json = JSON.parseObject(returnJson, JSONObject.class);
                    Integer status = json.getInteger("status_code");
                    String isOk = json.getString("status_txt");
                    if (status == 200 && isOk.equalsIgnoreCase("OK")) {
                        nsqTopicBeanList = new ArrayList<NsqTopicBean>();
                        JSONObject datajson = json.getJSONObject("data");
                        JSONArray topics = (JSONArray)datajson.getJSONArray("topics");
                        for (int i = 0; topics !=null && i < topics.size(); i++) {
                            List<NsqChannelBean> nsqChannelBeanList = new ArrayList<NsqChannelBean>();
                            NsqTopicBean nsqTopicBean = new NsqTopicBean();
                            JSONObject topic = topics.getJSONObject(i);
                            String topicName = topic.getString("topic_name");
                            int topicDepth = topic.getIntValue("depth");
                            int topicBackEndDepth = topic.getIntValue("backend_depth");
                            int message_count = topic.getIntValue("message_count");
                            JSONArray channels = topic.getJSONArray("channels");
                            nsqTopicBean.setTopicName(topicName);
                            nsqTopicBean.setBackend_depth(topicBackEndDepth);
                            nsqTopicBean.setDepth(topicDepth);
                            nsqTopicBean.setMessage_count(message_count);
                            for (int j = 0; channels != null && j < channels.size(); j++) {
                                NsqChannelBean nsqChannelBean = new NsqChannelBean();
                                JSONObject channel = channels.getJSONObject(j);
                                String channel_name = channel.getString("channel_name");
                                int depth = channel.getIntValue("depth");
                                int backend_depth = channel.getIntValue("backend_depth");
                                int in_flight_count = channel.getIntValue("in_flight_count");
                                int requeue_count = channel.getIntValue("requeue_count");
                                int timeout_count = channel.getIntValue("timeout_count");
                                int deferred_count = channel.getIntValue("deferred_count");
                                int msg = channel.getIntValue("message_count");
                                nsqChannelBean.setBackend_depth(backend_depth);
                                nsqChannelBean.setDef(deferred_count);
                                nsqChannelBean.setRequeue_count(requeue_count);
                                nsqChannelBean.setTime_out_count(timeout_count);
                                nsqChannelBean.setChannelName(channel_name);
                                nsqChannelBean.setMsg(msg);
                                nsqChannelBean.setInflt(in_flight_count);
                                nsqChannelBean.setDepth(depth);
                                nsqChannelBeanList.add(nsqChannelBean);
                            }
                            nsqTopicBean.setChannelBeans(nsqChannelBeanList);
                            nsqTopicBeanList.add(nsqTopicBean);
                        }//end for
                    }//end if

                }//end while
            }
        } catch (Exception e) {
            LOG.warn("获取nsqd节点topic 相关信息出现异常，异常出现的nsqd url为" + requestUrl,e);
        }
        LOG.info("获取nsqd节点topic 相关信息结束，信息为===》" + JSONObject.toJSONString(nsqTopicBeanList));
        return nsqTopicBeanList;
    }


    /**
     *  收集集群环境中所有的nsqd 的节点的topic信息
     * @param producers
     * @return Map<String, List<NsqTopicBean>>  为null,表示该lookup节点此刻网络不通畅
     */
    public static  Map<String /**lookupurl**/, Map<String/**nsqdURL**/, List<NsqTopicBean>>> getClusterTopic(Map<String/**LOOKUP URL**/, List<NsqProducerBean>> producers) {

        Map<String /**lookupurl**/, Map<String/**nsqdURL**/, List<NsqTopicBean>>> map0 = new HashMap<>();
        for (String lookupurl : producers.keySet()) {  //1.lookupurls
            Map<String/**nsqdURL**/, List<NsqTopicBean>> map = null;
            List<NsqProducerBean> producer = producers.get(lookupurl); //nsqds
            if (producer != null) {
                map = new HashMap<>();
                for (int i = 0; i < producer.size(); i++) {
                    List<NsqTopicBean> topicBeans = generateNsqdNodeInfoByNsqdUrl(producer.get(i).getHost());
                    map.put(producer.get(i).getHost(), topicBeans);
                }
            }
            map0.put(lookupurl, map);
        }
        LOG.info("the cluster of the nsq  topic info:"+JSONObject.toJSONString(map0));
        return map0;
    }

    /**
     * 生成公用的面板数据
     * @param lookupUrl
     * @param date
     * @param normal
     * @return
     */
    public static RecordView generateNormalVew(String lookupUrl, Date date,boolean normal) {
        RecordView recordView = new RecordView();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        recordView.addItem(new ItemView(DataType.ItemState.IGNORE, sdf.format(date)));
        recordView.addItem(new ItemView(normal==true?DataType.ItemState.OK:DataType.ItemState.WARNING,lookupUrl));
        return recordView;
    }


    /**
     *  根据配置文件，生成topic的配置信息比如topic的阻塞消息阀值，超时消息阀值等
     *  为了简单起见，不对topic进行去重校验，以及其他校验是否为真正的topic，出异常直接初始化失败
     *
     * @param threshold
     * @param defautBlock
     * @param defaultTimeout
     * @param defaultRequeue
     * @return  if is null ,it means monitor all topics of the lookup
     */
    public static Map<String/**topicName**/, NsqTopicConfig> msgTopicThresholdMapping(String threshold,int defautBlock,int defaultTimeout,int defaultRequeue) {

        Map<String, NsqTopicConfig> topicMap = null;
        if (StringUtil.isNullString(threshold)) {
          LOG.warn("the config nsq of the topic  param is null,it means is monitor all topic ");
            return topicMap;
        }
        topicMap = new HashMap<String, NsqTopicConfig>();
        String[] topicMapping = threshold.split(",");
        for (String topicThreshold : topicMapping) {
            NsqTopicConfig nsqTopicConfig = new NsqTopicConfig();

            String[] topiscs = topicThreshold.split(":");
            String topicName = topiscs[0];
            int topiscsLength = topiscs.length;
            int blockThreshold = 0;
            int timeoutThreshold = 0;
            int requeueThreshold = 0;

            if (topiscsLength == 1) {  //只写了topicName
                //使用默认的..
                blockThreshold = defautBlock;
                timeoutThreshold = defaultTimeout;
                requeueThreshold = defaultRequeue;
            } else if (topiscsLength == 2) {
                blockThreshold = Integer.parseInt(topiscs[1]);
                timeoutThreshold = defaultTimeout;
                requeueThreshold = defaultRequeue;

            } else if (topiscsLength == 3) {
                blockThreshold = Integer.parseInt(topiscs[1]);
                timeoutThreshold = Integer.parseInt(topiscs[2]);
                requeueThreshold = defaultRequeue;
            } else if (topiscsLength == 4) {
                blockThreshold = Integer.parseInt(topiscs[1]);
                timeoutThreshold = Integer.parseInt(topiscs[2]);
                requeueThreshold = Integer.parseInt(topiscs[3]);
            } else {
                throw new IllegalArgumentException("topic config character too much.." + topicThreshold);
            }
            nsqTopicConfig.setBlockMsgThreshold(blockThreshold);
            nsqTopicConfig.setRequeueMsgThreshold(requeueThreshold);
            nsqTopicConfig.setTimeoutMsgThreshold(timeoutThreshold);
            topicMap.put(topicName, nsqTopicConfig);
        }
        LOG.info("the nsq of the topic config info:" + JSONObject.toJSONString(topicMap));
        return topicMap;

    }

    /**
     * 针对一个nsqd 下的所有topic，将topic 对象转成监控需要的channelMonitor对象
     * @param nsqTopicBeans
     * @param NSQDURL
     * @param lookupurl
     * @return
     */
    public static List<NsqChannelMonitorBean> convertNsqtopicBean2NsqchannelMonitorBean(List<NsqTopicBean> nsqTopicBeans,String NSQDURL,String lookupurl) {

        List<NsqChannelMonitorBean> nsqChannelMonitorBeanList =  new ArrayList<>();

        if (nsqTopicBeans == null) {
            NsqChannelMonitorBean nsqChannelMonitorBean = new NsqChannelMonitorBean();
            nsqChannelMonitorBean.setLookuphost(lookupurl);
            nsqChannelMonitorBean.setNsqdhost(NSQDURL);
            nsqChannelMonitorBean.setLookupisOk(true);
            nsqChannelMonitorBean.setNsqdisOk(false);
            nsqChannelMonitorBeanList.add(nsqChannelMonitorBean);
            return nsqChannelMonitorBeanList;
        }
        for (int i = 0; i < nsqTopicBeans.size(); i++) {
            NsqTopicBean nsqTopicBean = nsqTopicBeans.get(i);
            List<NsqChannelBean> nsqChannelBeans = nsqTopicBean.getChannelBeans();
            NsqChannelMonitorBean nsqChannelMonitorBean = new NsqChannelMonitorBean();
            nsqChannelMonitorBean.setLookuphost(lookupurl);
            nsqChannelMonitorBean.setNsqdhost(NSQDURL);
            if (nsqChannelBeans.size() == 0 || nsqChannelBeans == null) { //说明这个topic下木有channel
                nsqChannelMonitorBean.setChannelName("");
                nsqChannelMonitorBean.setBlock_num(nsqTopicBean.getDepth());
            } else {
                for (int j = 0; j < nsqChannelBeans.size(); j++) {
                    NsqChannelBean nsqChannelBean = nsqChannelBeans.get(j);
                    nsqChannelMonitorBean.setBlock_num(nsqChannelBean.getBlocknum());
                    nsqChannelMonitorBean.setTimeout_num(nsqChannelBean.getTime_out_count());
                    nsqChannelMonitorBean.setRequeue_num(nsqChannelBean.getRequeue_count());
                    nsqChannelMonitorBean.setChannelName(nsqChannelBean.getChannelName());
                }
                nsqChannelMonitorBeanList.add(nsqChannelMonitorBean);
            }
        }
        LOG.info("before convert the topic info is :" + JSONObject.toJSONString(nsqTopicBeans));
        LOG.info("after convert the nsqchannel info is :" + JSONObject.toJSONString(nsqChannelMonitorBeanList));
        return nsqChannelMonitorBeanList;
    }

    /**
     * //针对每一个nsqdurl 来讲将nsqd下的所有topic 转成nsqdMonitor数据接收
     * @param nsqTopicBeans
     * @param lookupurl
     * @param nsqd
     * @return
     */
    public static List<NsqdMonitorBean> convertNsqTopicBean2NsqdMonitorBean( List<NsqTopicBean> nsqTopicBeans, String lookupurl, String nsqd) {
        List<NsqdMonitorBean> nsqdMonitorBeans = new ArrayList<>();
        if (nsqTopicBeans == null) { //说明该nsqd下 获取不到该信息，说明该nsqd节点死掉或者网络出现异常
            NsqdMonitorBean nsqdMonitorBean = new NsqdMonitorBean();
            nsqdMonitorBean.setNsqdHost(nsqd);
            nsqdMonitorBean.setLookupisOk(true);
            //nsqdMonitorBean.setLookupHost(lookupurl);
            nsqdMonitorBean.setNsqdisOk(false);
            nsqdMonitorBeans.add(nsqdMonitorBean);
            return nsqdMonitorBeans;
        }

        for (int i = 0; i < nsqTopicBeans.size(); i++) {
            NsqTopicBean nsqTopicBean = nsqTopicBeans.get(i);
            String topicName = nsqTopicBean.getTopicName();
            int topicdepth = nsqTopicBean.getDepth();
            List<NsqChannelBean> nsqChannelBeanList = nsqTopicBean.getChannelBeans();
            NsqdMonitorBean nsqdMonitorBean = new NsqdMonitorBean();
            nsqdMonitorBean.setLookupHost(lookupurl);
            nsqdMonitorBean.setNsqdHost(nsqd);
            nsqdMonitorBean.setLookupisOk(true);
            nsqdMonitorBean.setNsqdisOk(true);
            nsqdMonitorBean.setTopicName(topicName);
            if (nsqChannelBeanList == null || nsqChannelBeanList.size() == 0) {
                nsqdMonitorBean.setBlock_num(nsqTopicBean.getDepth()); //如何channel不存在，则将topic下的depth 作为block 数量
              //  nsqdMonitorBeans.add(nsqdMonitorBean);
            } else {
                int channel_blockMsg = 0 ;
                int chanel_timeoutMsg = 0 ;
                int channel_requeueMsg = 0;
                for (int j = 0; j < nsqChannelBeanList.size(); j++) {
                    NsqChannelBean nsqChannelBean = nsqChannelBeanList.get(j);
                    channel_blockMsg = channel_blockMsg + nsqChannelBean.getBlocknum();
                    chanel_timeoutMsg = chanel_timeoutMsg + nsqChannelBean.getTime_out_count();
                    channel_requeueMsg = channel_requeueMsg + nsqChannelBean.getRequeue_count();
                }
                nsqdMonitorBean.setBlock_num(channel_blockMsg);
                nsqdMonitorBean.setRequeue_num(channel_requeueMsg);
                nsqdMonitorBean.setTimeout_num(chanel_timeoutMsg);
            }//end else
            nsqdMonitorBeans.add(nsqdMonitorBean);
        }//
        if (nsqTopicBeans.size() != nsqdMonitorBeans.size()) {
            LOG.warn("convert nsq topic to nsqd ,the each of size is not equal, the nsq topic size is 【"+nsqTopicBeans.size()+"】,the nsqd  size is【"+nsqdMonitorBeans.size()+"】");
        }
        LOG.info("before convert the topic info is :"+JSONObject.toJSONString(nsqTopicBeans));
        LOG.info("after convert the nsqd info is :"+JSONObject.toJSONString(nsqdMonitorBeans));
        return nsqdMonitorBeans;
    }

    //一个lookup 下的监控所有的topic状态
    public static List<NsqTopicMonitorBean> convertTopic2NsqTopicMonitorBean(String lookupurl, Map<String/**nsqdURL**/, List<NsqTopicBean>> topics) {

        List<NsqTopicMonitorBean> nsqTopicMonitorBeans = new ArrayList<>();

        if (topics == null) {
            NsqTopicMonitorBean nsqTopicMonitorBean = new NsqTopicMonitorBean();
            nsqTopicMonitorBean.setLookupisOk(false);
            nsqTopicMonitorBean.setLookuphost(lookupurl);
            nsqTopicMonitorBeans.add(nsqTopicMonitorBean);
            return nsqTopicMonitorBeans;
        }

        //封装topic 跟ndqd monitor对应关系
        Map<String/**topicName**/, List<NsqdMonitorBean>> topicMap = new HashMap<String, List<NsqdMonitorBean>>();

        //遍历当前集群环境中的素有nsqd 节点
        for (String nsqdURL : topics.keySet()) {
            String nsqdurl = nsqdURL;
            List<NsqTopicBean> nsqTopicBeans = topics.get(nsqdurl);  // 每个nsqd 节点的topic 集合
            List<NsqdMonitorBean> nsqdMonitorBeans = NsqUtil.convertNsqTopicBean2NsqdMonitorBean(nsqTopicBeans, lookupurl, nsqdurl);

            for (int j = 0; j < nsqdMonitorBeans.size(); j++) {
                NsqdMonitorBean nsqdMonitorBean = nsqdMonitorBeans.get(j);
                String topicName = nsqdMonitorBean.getTopicName();
                if (!topicMap.containsKey(topicName)) {
                    List<NsqdMonitorBean> nsqdMonitorBeans1 = new ArrayList<NsqdMonitorBean>();
                    nsqdMonitorBeans1.add(nsqdMonitorBean);
                    topicMap.put(topicName, nsqdMonitorBeans1);
                } else {
                    List<NsqdMonitorBean> nsqdMonitorBeans1 = topicMap.get(topicName);
                    nsqdMonitorBeans1.add(nsqdMonitorBean);
                }
            }
        }

         LOG.info("the topic of the nsqd relationship is :"+JSONObject.toJSONString(topicMap));
        //解析topicMap，封装成topicMonitorBean
        for (String topicName /**topic的名称**/: topicMap.keySet()) {
            List<NsqdMonitorBean> nsqdMonitorBeanList = topicMap.get(topicName);
            NsqTopicMonitorBean nsqTopicMonitorBean = new NsqTopicMonitorBean();
            nsqTopicMonitorBean.setLookupisOk(true);
            nsqTopicMonitorBean.setTopicName(topicName);
            nsqTopicMonitorBean.setLookuphost(lookupurl);
            int nsqd_blockMsg = 0;
            int nsqd_timeoutMsg = 0;
            int nsqd_requeue = 0;
            for (int i = 0; i < nsqdMonitorBeanList.size(); i++) {
                NsqdMonitorBean nsqdMonitorBean = nsqdMonitorBeanList.get(i);
                nsqd_blockMsg = nsqd_blockMsg + nsqdMonitorBean.getBlock_num();
                nsqd_timeoutMsg = nsqd_timeoutMsg + nsqdMonitorBean.getTimeout_num();
                nsqd_requeue = nsqd_requeue + nsqdMonitorBean.getRequeue_num();
            }
            nsqTopicMonitorBean.setBlock_num(nsqd_blockMsg);
            nsqTopicMonitorBean.setTimeout_num(nsqd_timeoutMsg);
            nsqTopicMonitorBean.setRequeue_num(nsqd_requeue);
            nsqTopicMonitorBeans.add(nsqTopicMonitorBean);
            //end for map

        }
        LOG.info("before convert the single cluster of the topic info is :" + JSONObject.toJSONString(topics));
        LOG.info("after convert the nsq topic  info is :" + JSONObject.toJSONString(nsqTopicMonitorBeans));
        return nsqTopicMonitorBeans;
    }



    public static void main(String[] args) {
        String url = "http://10.0.200.51:1175/nodes";
        String nsqdurl = "http://10.0.200.51:1176/stats";
    //   getNsqproducers(url);
        generateNsqdNodeInfoByNsqdUrl(nsqdurl);
    }

}
