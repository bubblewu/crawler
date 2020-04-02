package com.bubble.crawler.service.impl;

import com.bubble.crawler.bean.VideoBean;
import com.bubble.crawler.service.ProcessService;
import com.bubble.crawler.util.HtmlUtil;
import com.bubble.crawler.util.RegexUtil;
import com.bubble.crawler.util.ToolKits;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * 优酷视频信息解析
 *
 * @author wugang
 * date: 2020-03-31 20:44
 **/
public class YouKuProcessServiceImpl implements ProcessService {
    private static final Logger LOGGER = LoggerFactory.getLogger(YouKuProcessServiceImpl.class);

    @Override
    public void process(VideoBean video) {
        LOGGER.info("start process {} from YouKu", video.getUrl());
        String content = video.getContent();
        Document doc = Jsoup.parse(content);
        // ID
        Pattern idPattern = Pattern.compile(getValue("IdRegex"), Pattern.DOTALL);
        video.setId(RegexUtil.match(video.getUrl(), idPattern, 1));
        // 剧名
        video.setName(HtmlUtil.getTextNodeByReplace(doc, getValue("Name"), "：", ""));
        // 评分
        String score = HtmlUtil.getFieldByRegex(doc, getValue("Score"), getValue("ScoreRegex"));
        video.setScore(Double.parseDouble(score));
        // 导演
        video.setDirectors(HtmlUtil.getText(doc, getValue("Director")));
        // 主演
        video.setName(HtmlUtil.getTextByReplace(doc, getValue("Actors"), "主演：", ""));
        // 标签（类型）
        video.setLabels(HtmlUtil.getTextByReplace(doc, getValue("Label"), "类型：", ""));
        video.setDesc(HtmlUtil.getText(doc, getValue("Desc")));

    }

    private static String getValue(String key) {
        return ToolKits.getPropertyValue("youku", key);
    }

}
