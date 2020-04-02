package com.bubble.crawler.util;

import com.bubble.crawler.util.exception.EmailSendException;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

/**
 * 邮件工具类
 *
 * @author wugang
 * date: 2020-04-02 17:26
 **/
public class EmailUtil {

    public static void sendEmail(String subject, String message) {
        //发送email
        HtmlEmail email = new HtmlEmail();
        try {
            // 这里是SMTP发送服务器的名字：qq的如下："smtp.qq.com"
            email.setHostName(ToolKits.getConfig("mail.host"));
            // 字符编码集的设置
            email.setCharset(ToolKits.getConfig("mail.encoding"));
            // 收件人的邮箱
            email.addTo(ToolKits.getConfig("mail.to"));
            // 设置是否加密
            email.setSSLOnConnect(false);
            email.setSmtpPort(Integer.parseInt(ToolKits.getConfig("mail.smtp.port")));
            // 发送人的邮箱
            email.setFrom(ToolKits.getConfig("mail.from"), ToolKits.getConfig("mail.nickname"));
            // 如果需要认证信息的话，设置认证：用户名-密码。分别为发件人在邮件服务器上的注册名称和授权码
            email.setAuthentication(ToolKits.getConfig("mail.username"), ToolKits.getConfig("mail.password"));
            // 要发送的邮件主题
            email.setSubject(subject);
            // 要发送的信息，由于使用了HtmlEmail，可以在邮件内容中使用HTML标签
            email.setMsg(message);
            // 发送
            email.send();
        } catch (EmailException e) {
            throw new EmailSendException("邮件发送失败!");
        }
    }

}
