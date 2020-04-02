package com.bubble.crawler.job.crontab;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Random;

/**
 * 打印时间测试
 *
 * @author wugang
 * date: 2020-04-02 15:24
 **/
public class PrintTimeJob implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrintTimeJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String nowTime = Instant.now().toString();
        LOGGER.info("当前时间：{}, Job-{}", nowTime, new Random().nextInt(100));
    }
}
