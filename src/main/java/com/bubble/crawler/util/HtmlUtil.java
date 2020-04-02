package com.bubble.crawler.util;

import com.google.common.collect.Lists;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 页面解析工具类
 *
 * @author wugang
 * date: 2020-04-01 19:16
 **/
public class HtmlUtil {

    public static String getFieldByRegex(Document doc, String selector, String regex) {
        Elements elements = doc.select(selector);
        if (elements.size() > 0) {
            String text = elements.get(0).text();
            Pattern scorePattern = Pattern.compile(regex, Pattern.DOTALL);
            return RegexUtil.match(text, scorePattern, 0);
        }
        return "";
    }

    public static String getTextNodeByReplace(Document doc, String selector, String target, String value) {
        Elements elements = doc.select(selector);
        if (elements.size() > 0) {
            Element e = elements.get(0);
            List<TextNode> textNodes = Optional.of(e.textNodes()).orElse(Lists.newArrayList());
            if (!textNodes.isEmpty()) {
                String text = textNodes.get(0).text();
                return text.replace(target, value);
            }
        }
        return "";
    }

    public static String getTextByReplace(Document doc, String selector, String target, String value) {
        Elements elements = doc.select(selector);
        if (elements.size() > 0) {
            Element e = elements.get(0);
            return e.text().replace(target, value);
        }
        return "";
    }

    public static String getText(Document doc, String selector) {
        Elements elements = doc.select(selector);
        if (elements.size() > 0) {
            Element e = elements.get(0);
            List<TextNode> textNodes = Optional.of(e.textNodes()).orElse(Lists.newArrayList());
            if (!textNodes.isEmpty()) {
                return textNodes.get(0).text();
            }
        }
        return "";
    }
}
