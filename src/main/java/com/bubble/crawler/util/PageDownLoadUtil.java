package com.bubble.crawler.util;

import com.bubble.crawler.util.db.RedisUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 页面下载Util
 *
 * @author wugang
 * date: 2020-03-31 18:01
 **/
public class PageDownLoadUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageDownLoadUtil.class);

    private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";

    private static CloseableHttpClient createClient() {
        HttpClientBuilder builder = HttpClients.custom();
        CloseableHttpClient client;

        /* 设置动态IP代理 */
        RedisUtil redisUtil = new RedisUtil();
        // 获取代理IP
        String ipPort = redisUtil.getSet("proxy");
        if (StringUtils.isNotBlank(ipPort)) {
            String[] arr = ipPort.split(":");
            String ip = arr[0];
            int port = Integer.parseInt(arr[1]);
            // 设置代理
            HttpHost proxy = new HttpHost(ip, port);
            client = builder.setProxy(proxy).build();
        } else {
            client = builder.build();
        }
        return client;
    }

    public static String getPageContent(String url) {
        CloseableHttpClient client = createClient();
        HttpGet request = new HttpGet(url);
        String content;
        request.setHeader("User-Agent", USER_AGENT);
        try {
            CloseableHttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            content = EntityUtils.toString(entity);
        } catch (IOException e) {
            content = "";
            LOGGER.error("execute http get error. url = [{}]", url, e);
        }
        return content;
    }

    public static void main(String[] args) {
        String url = "https://v.youku.com/v_show/id_XNDU4OTM3NzM0NA==.html?spm=a2h0k.11417342.soresults.dtitle&s=27eaee8934c44174bb83";
        LOGGER.info(getPageContent(url));
    }


}
