# 视频爬虫小项目

## 关于爬虫

### 主要模块
一般爬虫项目都包括下面5个方面：（其实也就是：采集层、处理层、存储层、展示层、监控层）
- 数据采集
涉及计技术：
下载：HttpClient
解析：HtmlCleaner + XPath、Jsoup + selector、正则表达式
- 数据分析
根据具体业务设计规则或算法，也可以接入搜索引擎，如Elasticsearch
- 数据存储
Redis、HDFS、Mysql、HBase等
- 报表管理
数据可视化
- 系统管理与监控
监控：Zookeeper
维护：邮件提醒
URL调度：Redis中的List列表、优先级队列

### 难点分析
- 网站采取反爬策略：
模拟各种浏览器来请求（不同的类型的、不同版本的浏览器来随机访问）；

- 网站模版定期变动：
避免硬编码解析网站。
不同配置文件配置不同网站的模版规则；
数据库存储不同网站的模版规则；

- 网站URL抓取失败：
使用HttpClient的默认处理方式（如尝试3次去获取，如失败再放弃）；
Storm实时解析日志，将失败的URL重新加入抓取仓库，一般超过3次就放弃；

- 网站频繁抓取IP被封：
购买代理IP库，随机获取IP来抓取；
部署多个应用分别抓取，降低单节点频繁访问；
设置每个页面抓取时间的间隔，降低被封的概率；

## 本项目主要实现
- 基于Redis的List列表或JDK优先级队列维护URL仓库；
- 基于多线程，使用HttpClient设置动态代理来下载数据；
- 基于Jsoup+selector、HtmlCleaner+XPath和正则来解析数据；
- 存储直接打印在客户端，未实现存储到数据库的操作；
- 基于quartz实现定时任务执行；
- 基于Zookeeper来实现任务的监控，如项目挂掉邮件报警；

