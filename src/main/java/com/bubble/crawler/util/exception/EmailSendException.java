package com.bubble.crawler.util.exception;

/**
 * 邮件发送异常
 *
 * @author wugang
 * date: 2020-04-02 18:56
 **/
public class EmailSendException extends RuntimeException {
    private static final long serialVersionUID = 3303963284890119390L;

    public EmailSendException() {
        super();
    }

    public EmailSendException(String message) {
        super(message);
    }

    public EmailSendException(String message, Throwable t) {
        super(message, t);
    }

}
