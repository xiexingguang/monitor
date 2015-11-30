package com.ec.monitor.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ecuser on 2015/11/26.
 */
public class UrlConnectionUtil {

    protected final static Logger LOG = LogManager.getLogger(UrlConnectionUtil.class);

    public static HttpURLConnection openGetConnection(String url) throws IOException {

        if (com.ec.monitor.util.StringUtil.isNullString(url)) {
            throw new IllegalArgumentException("url is null or \"\"");
        }

        if (!url.startsWith("http://")) {
            url = "http://" + url;
        }

        LOG.info("connection url is :"+url);
        URL urlGet = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urlGet.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("GET");
        conn.connect();
        return conn;
    }

}
