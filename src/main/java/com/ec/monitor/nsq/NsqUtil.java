package com.ec.monitor.nsq;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ec.monitor.bean.*;
import com.ec.monitor.util.StringUtil;
import com.ec.monitor.util.UrlConnectionUtil;
import com.ec.watcher.model.DataType;
import com.ec.watcher.model.ItemView;
import com.ec.watcher.model.RecordView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class NsqUtil {

    protected final static Logger LOG = LogManager.getLogger(NsqUtil.class);

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
    public static Map<String/**LOOKUP URL**/, List<NsqProducerBean>> getNsqProducersByLOOKupUrls(List<String> urls) {
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
    public  static Map<String, List<NsqTopicBean>> generateNsqdNodesByUrls(List<String> urls) {
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
        recordView.addItem(new ItemView(normal==true?DataType.ItemState.OK:DataType.ItemState.WARNING,lookupUrl));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        recordView.addItem(new ItemView(DataType.ItemState.IGNORE, sdf.format(date)));
        return recordView;


    }


    public static Map<String/**topicName**/, Integer> msgTopicThresholdMapping(String threshold) {
        if (StringUtil.isNullString(threshold)) {
            throw new IllegalStateException("配置的topic阀值参数不存在!");
        }
        Map<String, Integer> topicMap = new HashMap<String, Integer>();
        String[] topicMapping = threshold.split(",");
        for (String topicThreshold : topicMapping) {
            String[] topiscs = topicThreshold.split(":");
            topicMap.put(topiscs[0], Integer.parseInt(topiscs[1]));
        }
        return topicMap;

    }

    public static NsqChannelMonitorBean convertNsqtopicBean2NsqchannelMonitorBean(NsqTopicBean nsqTopicBean) {
        NsqChannelMonitorBean nsqChannelMonitorBean = null;
        return nsqChannelMonitorBean;
    }

    public static NsqdMonitorBean convertNsqChannelMontorBean2NsqdMonitorBean(NsqChannelMonitorBean nsqChannelMonitorBean) {
        NsqdMonitorBean nsqdMonitorBean = null;
        return nsqdMonitorBean;
    }

    public static NsqTopicMonitorBean convertNsqdMonitorBean2NsqTopicMonitorBean(NsqdMonitorBean nsqdMonitorBean) {
        NsqTopicMonitorBean nsqTopicMonitorBean = null;
        return nsqTopicMonitorBean;
    }



    public static void main(String[] args) {
        String url = "http://10.0.200.51:1175/nodes";
        String nsqdurl = "http://10.0.200.51:1176/stats";
    //   getNsqproducers(url);
        generateNsqdNodeInfoByNsqdUrl(nsqdurl);


    }

}
