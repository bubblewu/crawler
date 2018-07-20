package com.bubble.crawler.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 格式转换工具
 *
 * @author wugang
 * date: 2018-07-19 09:44
 **/
public class FormatUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FormatUtils.class);

    public static String xml2String(String xml) {
        return xml2Json(xml).toString();
    }

    /**
     * 将xml数据转换为json格式数据
     *
     * @param xml xml格式数据
     * @return JSONObject(来自fastJson)
     */
    public static JSONObject xml2Json(String xml) {
        JSONObject json = new JSONObject();
        try {
            Document doc = DocumentHelper.parseText(xml);
            Element root = doc.getRootElement(); //获取根节点元素对象
            parseNodes(json, root);
            return json;
        } catch (DocumentException e) {
            LOGGER.error("parse xml data error. {}", e);
        }
        return new JSONObject();
    }

    /**
     * 遍历处理
     *
     * @param json 处理后组装的json对象
     * @param node 需处的xml元素
     */
    private static void parseNodes(JSONObject json, Element node) {
        String nodeName = node.getName();
        // 判断json中是否已经存在该node
        if (json.containsKey(nodeName)) {
            // 该元素在同级下有多个的情况
            Object object = json.get(nodeName);
            JSONArray array;
            if (object instanceof JSONArray) {
                array = (JSONArray) object;
            } else {
                array = new JSONArray();
                array.add(object);
            }
            // 获取该元素下所有子元素
            List<Element> elementList = node.elements();
            // 该元素无子元素，获取元素的值
            if (elementList.isEmpty()) {
                String value = node.getTextTrim();
                array.add(value);
                json.put(nodeName, array);
            }
            //有子元素,递归遍历所有子元素
            JSONObject newJson = new JSONObject();
            elementList.forEach(element -> parseNodes(newJson, element));
            array.add(newJson);
            json.put(nodeName, array);
            return;
        }

        //该元素同级下第一次遍历,获取该元素下所有子元素
        List<Element> elementList = node.elements();
        // 该元素无子元素，获取元素的值
        if (elementList.isEmpty()) {
            String value = node.getTextTrim();
            json.put(nodeName, value);
            return;
        }
        //有子节点，新建一个JSONObject来存储该节点下子节点的值
        JSONObject newJson = new JSONObject();
        elementList.forEach(element -> parseNodes(newJson, element));
        json.put(nodeName, newJson);
        return;
    }

}
