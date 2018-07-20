package com.bubble.crawler.util;

import org.junit.Test;

/**
 * 文件操作工具类测试
 *
 * @author wugang
 * date: 2018-07-19 16:18
 **/
public class FileUtilsTest {

    /**
     * 加载和指定类同级的文件
     */
    @Test
    public void testLoad() {
        String text = FileUtils.load(FileUtilsTest.class, "gitignore.txt");
        System.out.println(text);
    }

    /**
     * 加载resources目录下的文件
     */
    @Test
    public void testLoadResourcesFile() {
        String text = FileUtils.loadResourcesFile("search.json");
        System.out.println(text);
    }

    @Test
    public void testRead() {
        FileUtils.read("/Users/wugang/workspace/crawler/.gitignore").forEach(System.out::println);
    }

    @Test
    public void testWriter() {
        boolean append = true;
        String content = "测试写入文件";
        FileUtils.writer("/Users/wugang/workspace/crawler/src/test/java/com/bubble/crawler/util/test.txt", content, append);
    }

}
