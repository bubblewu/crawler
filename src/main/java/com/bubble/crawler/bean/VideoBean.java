package com.bubble.crawler.bean;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

/**
 * 视频实体类
 *
 * @author wugang
 * date: 2020-03-31 18:41
 **/
public class VideoBean implements Serializable {
    private static final long serialVersionUID = -5080764705104066072L;

    private String id; // 视频ID
    private String name; // 视频名称
    private Double score; // 评分
    private Long hotScore; // 热度
    private String labels; // 标签：多个用逗号分割

    private String directors; // 导演：多个用逗号分割
    private String actors; // 主演：多个用逗号分割
    private String desc; // 内容简介

    private Integer commentCount; // 评论数

    private String url; // URL地址
    private String content; // 源文件内容

    // 存储电视剧url（包含列表url和详情页url）
    private List<String> urlList = Lists.newArrayList();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Long getHotScore() {
        return hotScore;
    }

    public void setHotScore(Long hotScore) {
        this.hotScore = hotScore;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getDirectors() {
        return directors;
    }

    public void setDirectors(String directors) {
        this.directors = directors;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getUrlList() {
        return urlList;
    }

    public void setUrlList(List<String> urlList) {
        this.urlList = urlList;
    }

    @Override
    public String toString() {
        return "VideoBean{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", score=" + score +
                ", hotScore=" + hotScore +
                ", labels='" + labels + '\'' +
                ", directors='" + directors + '\'' +
                ", actors='" + actors + '\'' +
                ", desc='" + desc + '\'' +
                ", commentCount=" + commentCount +
                ", url='" + url + '\'' +
//                ", content='" + content + '\'' +
//                ", urlList=" + urlList +
                '}';
    }
}
