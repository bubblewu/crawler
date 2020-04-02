package com.bubble.crawler.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式
 *
 * @author wugang
 * date: 2020-04-01 14:59
 **/
public class RegexUtil {

    public static String match(String content, Pattern pattern, int groupNo) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(groupNo).trim();
        }
        return "0";
    }

    public static void main(String[] args) {
        System.out.println(match("评论数：2000", Pattern.compile("(?<=评论数：)[\\d,]+"), 0));
        System.out.println(match("评分： 7.7", Pattern.compile("(?<=评分： )[\\d.\\d]+"), 0));
    }

}
