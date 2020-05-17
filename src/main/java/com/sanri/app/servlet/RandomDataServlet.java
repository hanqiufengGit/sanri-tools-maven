package com.sanri.app.servlet;

import com.sanri.app.classloader.RandomDataService;
import com.sanri.frame.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RequestMapping("/random")
public class RandomDataServlet {
    RandomDataService randomDataService = new RandomDataService();

    /**
     * 随机填充数据
     * @param className
     * @param classloaderName
     * @return
     * @throws ClassNotFoundException
     */
    public Object randomData(String className,String classloaderName) throws ClassNotFoundException {
        return randomDataService.randomData(className,classloaderName);
    }

    /**
     * 随机填充列表数据
     * @param className
     * @param classloaderName
     * @return
     * @throws ClassNotFoundException
     */
    public List<Object> randomListData(String className,String classloaderName) throws ClassNotFoundException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Object randomData = randomDataService.randomData(className, classloaderName);
            list.add(randomData);
        }
        return list;
    }
}
