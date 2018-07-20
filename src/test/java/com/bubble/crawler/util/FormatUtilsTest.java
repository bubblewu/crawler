package com.bubble.crawler.util;

import com.alibaba.fastjson.JSONObject;
import org.junit.Before;
import org.junit.Test;

/**
 * 格式转换工具类测试
 *
 * @author wugang
 * date: 2018-07-19 18:32
 **/
public class FormatUtilsTest {
    private String xml;

    @Before
    public void setUp() {
        xml = FileUtils.loadResourcesFile("hebei.xml");
    }

    @Test
    public void testXml2Json() {
        JSONObject json = FormatUtils.xml2Json(xml);
        System.out.println(json);
    }

    @Test
    public void testXml2String() {
        String json = FormatUtils.xml2String(xml);
        System.out.println(json);
    }

}
