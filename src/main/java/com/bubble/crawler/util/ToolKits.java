package com.bubble.crawler.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常见工具类
 *
 * @author wugang
 * date: 2020-04-02 14:43
 **/
public class ToolKits {

    public static void sleep(long millions) {
        try {
            TimeUnit.MILLISECONDS.sleep(millions);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取顶级域名
     * 如：http://list.iqiyi.com/www/2/-------------10-3-1---.html 对应：iqiyi.com
     *
     * @param url url
     * @return 顶级域名
     */
    public static String getTopDomain(String url) {
        try {
            String host = new URL(url).getHost().toLowerCase();
            Pattern pattern = Pattern.compile("[^\\.]+(\\.com\\.cn|\\.net\\.cn|\\.org\\.cn|\\.gov\\.cn|\\.com|\\.net|\\.cn|\\.org|\\.cc|\\.me|\\.tel|\\.mobi|\\.asia|\\.biz|\\.info|\\.name|\\.tv|\\.hk)");
            Matcher matcher = pattern.matcher(host);
            while (matcher.find()) {
                return matcher.group();
            }
        } catch (MalformedURLException e) {
            System.out.println("get top domain error, url=" + url);
        }
        return "";
    }

    // 读取公共配置文件
    public static String getConfig(String key) {
        return getPropertyValue("config", key);
    }

    /**
     * 读取配置文件的属性
     *
     * @param fileName 配置文件名称，如target/classes/youku.properties对应youku
     * @param key      属性
     * @return 属性值
     */
    public static String getPropertyValue(String fileName, String key) {
        try {
            Locale locale = Locale.getDefault();
            ResourceBundle bundle = ResourceBundle.getBundle(fileName, locale);
//            return new String(bundle.getString(key).getBytes(), "UTF-8");
            return new String(bundle.getString(key).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        } catch (MissingResourceException mre) {
            return "";
        }
    }

    public static void main(String[] args) {
        System.out.println(getPropertyValue("youku", "ScoreRegex"));
    }

}
