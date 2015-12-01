package com.ec.monitor.watcher;

import com.ec.monitor.properties.Constants;
import com.ec.watcher.model.RecordView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ecuser on 2015/11/25.
 * watcher　基类，基类维护了recordViewsMap
 * 所有监控模块公用
 *
 */
public abstract  class BaseWatcher {

     //内部维护了所有模块的监控数据，key为模块名称，需要每隔多久扫描该数据结构，定时清理，防止内存撑爆
    protected final static Map<String/**模块名称or reqort名称**/, Map<String/**需要集中显示的数据**/,List<RecordView>>> recordViewsMap =
              new HashMap<String/*模块名称or reqort名称*/, Map<String,List<RecordView>>>();

 //  private ScheduledExecutorService clean_map = Executors.newSingleThreadScheduledExecutor();

    /**
     * //TODO定时清理历史数据
     */
    private void cleanHistoryData() {

    }

    public static Map<String, Map<String, List<RecordView>>> getRecordViewsMap() {
        return recordViewsMap;
    }

    /**
     * 添加监控数据
     * @param moduleName 模块名称
     * @param views
     */
    protected void addView(String moduleName, Map<String/**需要集中显示的数据，lookupurl**/,List<RecordView>> views) {
        for (String lookup : views.keySet()) {
            List<RecordView> recordViews = views.get(lookup);
            if (recordViews.size() > Constants.protectedOverLoadNum) {  //过载保护，防止撑爆内存
                recordViews.remove(0);
            }
        }
        recordViewsMap.put(moduleName, views);
    }
    protected abstract void generatedViewData();

}
