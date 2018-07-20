package com.bubble.crawler.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

/**
 * 粘贴板工具类
 *
 * @author wugang
 * date: 2018-07-19 19:09
 **/
public class ClipboardUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClipboardUtils.class);

    /**
     * 将输入的内容复制到系统粘贴板
     *
     * @param data 输入数据
     */
    public static void copy2SysClipboard(String data) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard(); // 获取系统剪贴板
        Transferable tText = new StringSelection(data);
        clipboard.setContents(tText, null);
        LOGGER.info("您输入的内容已经复制到系统粘贴板上.");
    }

    /**
     * 获取系统粘贴板上的文字
     *
     * @return 粘贴板上的文字
     */
    public static String getClipboardString() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable content = clipboard.getContents(null); //从系统剪切板中获取数据
        if (content != null) {
            if (content.isDataFlavorSupported(DataFlavor.stringFlavor)) { //判断是否为文本类型
                try {
                    return content.getTransferData(DataFlavor.stringFlavor).toString().trim();
                } catch (Exception e) {
                    LOGGER.error("系统粘贴板上内容不是文本.");
                    e.printStackTrace();
                }
            }
        } else {
            LOGGER.error("系统粘贴板上无内容");
        }
        return null;
    }
}
