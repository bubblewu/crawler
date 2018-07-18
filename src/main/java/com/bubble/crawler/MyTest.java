package com.bubble.crawler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wugang
 * date: 2018-07-02 10:05
 **/
public class MyTest {

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("1", "1");
        map.put("2", "2");
        String a = map.remove("1");
        System.out.println("a = " + a);
        for (String m : map.keySet()) {
            System.out.println(map.get(m));
        }
    }

}
