package com.bubble.crawler.job.crontab;

import com.bubble.crawler.util.ToolKits;
import com.bubble.crawler.util.db.RedisUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * 向redis 添加分类url
 *
 * @author wugang
 * date: 2020-04-02 15:07
 **/
public class AddUrlJob implements Job {
    private final RedisUtil redisUtil = new RedisUtil();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        List<String> list = redisUtil.query(ToolKits.getConfig("StartUrl"), 0, -1);
        list.forEach(url -> redisUtil.add(ToolKits.getConfig("HighKey"), url));
    }
}
