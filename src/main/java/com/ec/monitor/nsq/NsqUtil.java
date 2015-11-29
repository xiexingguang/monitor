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
     * @return
     */
    public static List<NsqProducerBean> getNsqproducers(String url) {
        LOG.info("获取lookup 所有nsqd 节点信息开始====》请求url为:"+url);
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
           // throw new RuntimeException("根据lookup获取nsqd节点信息异常，异常lookupurl地址为"+url,e);
        }finally {
           /* try {
                inputStream.close();
                bufferedReader.close();
            } catch (IOException e) {
                inputStream = null;
                bufferedReader = null;
            }*/
        }
        LOG.info("获取nsqd节点信息完成，所有nsqd 信息为===》"+JSONObject.toJSONString(nsqProducerBeans));
        return nsqProducerBeans;

    }


    /**
     * 根据lookupurls地址，获取lookup下监听的nsqd  地址，一般为多个
     *
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




    public static List<NsqTopicBean> generateNsqdNodeInfoByNsqdUrl(String url) {
         LOG.info("获取nsqd节点topic 相关信息，获取nsqd节点url为============》"+url);
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        String requestUrl = url + "?format=json";
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
        } catch (IOException e) {
            LOG.warn("获取nsqd节点topic 相关信息出现异常，异常出现的nsqd url为" + requestUrl,e);
        }finally {
            /*try {
              //  inputStream.close(); // 不需要，自己直接关闭了...??
               // bufferedReader.close();
            } catch (IOException e) {
                inputStream = null;
                bufferedReader = null;
            }*/
        }
        LOG.info("获取nsqd节点topic 相关信息结束，信息为===》"+JSONObject.toJSONString(nsqTopicBeanList));
        return nsqTopicBeanList;
    }

    //urls，表示多个nsqd的请求地址
    public  static Map<String, List<NsqTopicBean>> generateNsqdNodesByUrls(String lookupurls) {
        if (StringUtil.isNullString(lookupurls)) {
            throw new IllegalArgumentException("错误的lookupurls 地址" + lookupurls);
        }
        List<String> urls = Arrays.asList(lookupurls.split(","));
        Map<String, List<NsqTopicBean>> nodes = new HashMap<String, List<NsqTopicBean>>();
        for (int i = 0; i < urls.size(); i++) {
            String key = urls.get(i);
            List<NsqTopicBean> nsqTopicBeans = generateNsqdNodeInfoByNsqdUrl(urls.get(i));
            nodes.put(key, nsqTopicBeans);
        }
        return nodes;
    }

    //获取消息topic总数
  //  public NsqTopicMonitorBean


    public static RecordView generateNormalVew(String lookupUrl, Date date,boolean normal) {
        RecordView recordView = new RecordView();
        recordView.addItem(new ItemView(DataType.ItemState.IGNORE, sdf.format(date)));
        recordView.addItem(new ItemView(normal==true?DataType.ItemState.OK:DataType.ItemState.WARNING,lookupUrl));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return recordView;


    }


    public static Map<String/**topicName**/, NsqTopicConfig> msgTopicThresholdMapping(String threshold,int defautBlock,int defaultTimeout,int defaultRequeue) {
        if (StringUtil.isNullString(threshold)) {
            throw new IllegalStateException("配置的topic阀值参数不存在!");
        }
        Map<String, NsqTopicConfig> topicMap = new HashMap<String, NsqTopicConfig>();
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
            }else if (topiscsLength == 2) {
                blockThreshold = Integer.parseInt(topiscs[1]);
                timeoutThreshold = defaultTimeout;
                requeueThreshold = defaultRequeue;

            }else if (topiscsLength == 3) {
                blockThreshold = Integer.parseInt(topiscs[1]);
                timeoutThreshold =  Integer.parseInt(topiscs[2]);
                requeueThreshold = defaultRequeue;
            }else if (topiscsLength == 4) {
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
        return topicMap;

    }

    //一个nsqd下的所有topics,转成对应的channelMonitorbean
    public static List<NsqChannelMonitorBean> convertNsqtopicBean2NsqchannelMonitorBean(List<NsqTopicBean> nsqTopicBeans,String NSQDURL,String lookupurl) {

        List<NsqChannelMonitorBean> nsqChannelMonitorBeanList = null;
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
                //  nsqChannelMonitorBean.set

            } else {
                for (int j = 0; j < nsqChannelBeans.size(); j++) {
                    NsqChannelBean nsqChannelBean = nsqChannelBeans.get(j);
                    nsqChannelMonitorBean.setBlock_num(nsqChannelBean.getBlocknum());
                    nsqChannelMonitorBean.setTimeout_num(nsqChannelBean.getTime_out_count());
                    nsqChannelMonitorBean.setRequeue_num(nsqChannelBean.setRequeue_count());
                    nsqChannelMonitorBean.setChannelName(nsqChannelBean.getChannelName());
                }
                nsqChannelMonitorBeanList.add(nsqChannelMonitorBean);
            }
        }

        return nsqChannelMonitorBeanList;
    }

    //将nsqd下的所有channelmonitor转成成NsqdMonitorBean
    public static List<NsqdMonitorBean> convertNsqTopicBean2NsqdMonitorBean( List<NsqTopicBean> nsqTopicBeans, String lookupurl, String nsqd) {
        List<NsqdMonitorBean> nsqdMonitorBeans = null;
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
            if (nsqChannelBeanList == null || nsqChannelBeanList.size() == 0) {
                nsqdMonitorBean.setBlock_num(nsqTopicBean.getDepth()); //如何channel不存在，则将topic下的depth 作为block 数量
                nsqdMonitorBeans.add(nsqdMonitorBean);
            } else {
                int channel_blockMsg = 0 ;
                int chanel_timeoutMsg = 0 ;
                int channel_requeueMsg = 0;
                for (int j = 0; j < nsqChannelBeanList.size(); j++) {
                    NsqChannelBean nsqChannelBean = nsqChannelBeanList.get(i);
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

        return nsqdMonitorBeans;
    }

    //一个lookup 下的监控所有的topic状态
    public static List<NsqTopicMonitorBean> convertNsqdMonitorBean2NsqTopicMonitorBean(String lookupur) {
        List<NsqTopicMonitorBean> nsqTopicMonitorBeans = null;
        List<NsqProducerBean> nsqProducerBeans = NsqUtil.getNsqproducers(lookupur);
        Map<String, List<NsqdMonitorBean>> topicMap = new HashMap<String, List<NsqdMonitorBean>>();
        boolean lookupIsOk = false;
        if (nsqProducerBeans == null) {
            NsqTopicMonitorBean nsqTopicMonitorBean = new NsqTopicMonitorBean();
            nsqTopicMonitorBean.setLookupisOk(lookupIsOk);
            nsqTopicMonitorBeans.add(nsqTopicMonitorBean);
            return nsqTopicMonitorBeans;
        }
        lookupIsOk = true;
        for (int i = 0; i < nsqProducerBeans.size(); i++) {  //遍历当前集群环境中的nsqd节点
            NsqProducerBean nsqProducerBean = nsqProducerBeans.get(i);
            String nsqdurl = nsqProducerBean.getGetNodesStatsUrl();
            List<NsqTopicBean> topicBeans = NsqUtil.generateNsqdNodeInfoByNsqdUrl(nsqdurl); // 又请求了很多遍
            List<NsqdMonitorBean> nsqdMonitorBeans = NsqUtil.convertNsqTopicBean2NsqdMonitorBean(topicBeans, lookupur, nsqdurl);
            for (int j = 0; j < nsqdMonitorBeans.size(); j++) {
                NsqdMonitorBean nsqdMonitorBean = nsqdMonitorBeans.get(i);
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

        }//end for

        //解析topicmap 生成nsqtopicbean,
        for (Map.Entry<String, List<NsqdMonitorBean>> entry : topicMap.entrySet()) {
            String keyTopic = entry.getKey();
            List<NsqdMonitorBean> nsqdMonitorBeanList = entry.getValue();
            //一个topic对应多个来自不同节点的nsqd monitor 即来自不同nsqd 节点
            NsqTopicMonitorBean nsqTopicMonitorBean = new NsqTopicMonitorBean();
            nsqTopicMonitorBean.setLookupisOk(lookupIsOk);
            nsqTopicMonitorBean.setLookuphost(lookupur);
            int nsqd_blockMsg=0;
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
        }//end for map
        return nsqTopicMonitorBeans;
    }



    public static void main(String[] args) {
        String url = "http://10.0.200.51:1175/nodes";
        String nsqdurl = "http://10.0.200.51:1176/stats";
    //   getNsqproducers(url);
        generateNsqdNodeInfoByNsqdUrl(nsqdurl);


    }

}
