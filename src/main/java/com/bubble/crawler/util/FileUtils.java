package com.bubble.crawler.util;

import com.bubble.crawler.FileProcessingException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * 文件操作工具类
 *
 * @author wugang
 * date: 2018-07-19 10:06
 **/
public class FileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    private static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 读取文件内容
     *
     * @param filePath 路径
     * @return 内容集合
     */
    public static List<String> read(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            LOGGER.error("read file [{}] error.", filePath);
            throw new FileProcessingException("read file error.", e);
        }
    }

    /**
     * 将内容写入文件
     *
     * @param path    文件
     * @param content 内容
     */
    public static void writer(String path, String content) {
        writer(path, content.getBytes());
    }

    /**
     * 将内容写入文件
     *
     * @param path     文件
     * @param contents 内容
     */
    public static void writer(String path, byte[] contents) {
        try {
            Files.write(Paths.get(path), contents);
        } catch (IOException e) {
            LOGGER.error("writer in file [{}] error.", path);
            throw new FileProcessingException("write in file error.", e);
        }
    }

    /**
     * 加载resources目录下的文件
     *
     * @param fileName 文件名
     * @return 文件内容字符串
     */
    public static String loadResourcesFile(String fileName) {
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
            return IOUtils.toString(inputStream, DEFAULT_CHARSET);
        } catch (IOException e) {
            LOGGER.error("load resources file [{}] error.", fileName);
            throw new FileProcessingException("load resources file error.", e);
        }
    }

    /**
     * 加载和指定类同级的文件
     *
     * @param clazz    类文件
     * @param fileName 文件名
     * @return 文件内容字符串
     */
    public static String load(Class<?> clazz, String fileName) {
        return load(clazz, fileName, DEFAULT_CHARSET);
    }

    /**
     * 加载和指定类同级的文件
     *
     * @param clazz    类文件
     * @param fileName 文件名
     * @param charset  编码
     * @return 文件内容字符串
     */
    public static String load(Class<?> clazz, String fileName, String charset) {
        try {
            String path = getClassPath(clazz) + "/" + fileName;
            return IOUtils.toString(loadStream(path), charset);
        } catch (IOException e) {
            LOGGER.error("stream convert to string error.");
            throw new FileProcessingException("stream convert to string error.", e);
        }
    }

    /**
     * 在使用ClassLoader.getResourceAsStream时，路径直接使用相对于classpath的绝对路径,不能以’/'开头
     */
    private static InputStream loadStream(String filePath) {
        return FileUtils.class.getClassLoader().getResourceAsStream(filePath);
    }

    private static String getClassPath(Class<?> clazz) {
        return clazz.getPackage().getName().replace(".", "/");
    }

}
