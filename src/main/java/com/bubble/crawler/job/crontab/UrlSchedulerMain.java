package com.bubble.crawler.job.crontab;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 分类url定时抓取job
 *
 * @author wugang
 * date: 2020-04-02 15:14
 **/
public class UrlSchedulerMain {

    public static void main(String[] args) throws SchedulerException, InterruptedException {
        // 1、创建调度器Scheduler
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        // 2、创建JobDetail实例，并与PrintTimeJob类绑定(Job执行内容)
        JobDetail jobDetail = JobBuilder.newJob(PrintTimeJob.class).withIdentity("job1", "group1").build();
        // 3、构建Trigger实例,每隔1s执行一次：Trigger是Quartz的触发器，会去通知Scheduler何时去执行对应Job。
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger1")
                .startNow() // 立即生效
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInSeconds(10) // 每隔1s执行一次
                                .repeatForever()
                )
                .build(); // 一直执行

        /* 定时执行：每天的整点（分钟级别）执行定时任务 */
        Instant startTime = Instant.now();
        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger1", "triggerGroup1")
                .usingJobData("trigger1", "这是jobDetail1的trigger")
                .startNow() // 立即生效
                .startAt(Date.from(startTime))
                .endAt(Date.from(startTime.plus(30, ChronoUnit.DAYS)))
                .withSchedule(CronScheduleBuilder.cronSchedule("0 * * * * ? 2020"))
                .build();

        // 4、执行
//        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.scheduleJob(jobDetail, cronTrigger);

        System.out.println("--------scheduler start ! ------------");
        scheduler.start();

        // 睡眠
//        TimeUnit.MINUTES.sleep(1);
//        scheduler.shutdown();
//        System.out.println("--------scheduler shutdown ! ------------");
    }

}
