package com.bubble.crawler.util;

import org.junit.Test;

/**
 * 粘贴板工具类
 *
 * @author wugang
 * date: 2018-07-19 19:09
 **/
public class ClipboardUtilsTest {

    @Test
    public void testCopy2Clipboard() {
        ClipboardUtils.copy2SysClipboard("Good Job!");
    }

    @Test
    public void testGetClipboardString() {
        String text = ClipboardUtils.getClipboardString();
        System.out.println(text);
    }

}
