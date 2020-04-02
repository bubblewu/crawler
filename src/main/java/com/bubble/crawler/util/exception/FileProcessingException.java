package com.bubble.crawler.util.exception;

/**
 * 文件处理异常
 *
 * @author wugang
 * date: 2018-07-20 10:12
 **/
public class FileProcessingException extends RuntimeException {
    private static final long serialVersionUID = -2802985455160764996L;

    public FileProcessingException() {
        super();
    }

    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable t) {
        super(message, t);
    }

}
