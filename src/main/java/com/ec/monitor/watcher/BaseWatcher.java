package com.ec.monitor.watcher;

import com.ec.watcher.model.RecordView;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by ecuser on 2015/11/25.
 * watcher　基类，基类维护了recordViewsMap
 *
 */
@Component
public abstract  class BaseWatcher {

     //内部维护了所有模块的监控数据，key为模块名称，需要每隔多久扫描该数据结构，定时清理，防止内存撑爆
    private final static Map<String/*模块名称or reqort名称*/, Map<String/**需要集中显示的数据**/,List<RecordView>>> recordViewsMap =
              new HashMap<String/*模块名称or reqort名称*/, Map<String,List<RecordView>>>();

    private ScheduledExecutorService clean_map = Executors.newSingleThreadScheduledExecutor();


    public Map<String/*模块名称or reqort名称*/, Map<String/**需要集中显示的数据**/,List<RecordView>>> getRecordViews() {
        generatedViewData();
        recordViewsMap.size();
        return recordViewsMap;
    }


    /**
     * //TODO
     */
    private void cleanHistoryData() {

    }


    /**
     * 添加监控数据
     * @param key
     * @param recordView
     */
   /* public void add(String key, RecordView recordView) {
        List<RecordView> recordViews = recordViewsMap.get(key);
        if(recordViews == null){
            recordViews = new LinkedList<RecordView>();
        }else{
            if(recordViews.size() >=10){
                recordViews.remove(0);
            }
        }
        recordViews.add(recordView);
        recordViewsMap.put(key, recordViews);
    }*/
    protected abstract void generatedViewData();

}
