package com.sanri.app.classloader;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.util.NetUtils;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.util.ReflectionUtils;
import sanri.utils.RandomUtil;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * 随机数据生成
 */
public class RandomDataService {

    public ClassLoaderManager classLoaderManager = ClassLoaderManager.getInstance();

    public Object randomData(String className,ClassLoader classLoader) throws ClassNotFoundException {
        Class<?> clazz = classLoader.loadClass(className);
        return populateDataComplex(clazz);
    }

    public Object randomData(String className,String classloaderName) throws ClassNotFoundException {
        ExtendClassloader extendClassloader = classLoaderManager.get(classloaderName);
        return randomData(className,extendClassloader);
    }

    public Object populateDataComplex(Class<?> clazz) {
        Object object = ReflectUtils.newInstance(clazz);

        PropertyDescriptor[] beanSetters = ReflectUtils.getBeanSetters(clazz);
        for (PropertyDescriptor beanSetter : beanSetters) {
            Method writeMethod = beanSetter.getWriteMethod();
            String columnName = beanSetter.getName();
            Class<?> propertyType = beanSetter.getPropertyType();
            if(ClassUtils.isPrimitiveOrWrapper(propertyType) || propertyType == String.class || propertyType == Date.class ){
                if(propertyType == String.class){
                    int randomLength = RandomUtils.nextInt(20);
                    while (randomLength == 0){
                        randomLength = RandomUtils.nextInt(20);
                    }
                    String value = RandomStringUtils.randomAlphabetic(randomLength);
                    String lowerCase = columnName.toLowerCase();
                    if(lowerCase.contains("ip")){
                        value = "114.114.114.114";
                    }else
                    if(lowerCase.contains("idcard")){
                        value = RandomUtil.idcard();
                    }else
                    if(lowerCase.contains("mail")){
                        value = RandomUtil.email(30);
                    }else
                    if(lowerCase.contains("phone") ){
                        value = RandomUtil.phone();
                    }else
                    if(lowerCase.contains("name") || lowerCase.contains("user")){
                        value = RandomUtil.username();
                    }else
                    if(lowerCase.contains("address")){
                        value = RandomUtil.address();
                    }else
                    if(lowerCase.contains("uuid")){
                        value = UUID.randomUUID().toString().replace("-","");
                    }else
                    if(lowerCase.contains("job")){
                        value = RandomUtil.job();
                    }else
                    if(lowerCase.contains("status") || lowerCase.contains("state")){
                        value = RandomUtil.status("1","2","3");
                    }else
                    if(lowerCase.contains("time") || (columnName.contains("date") && !columnName.equals("update"))){
                        // 这里可以做时间格式转换,获取字段上的配置
                        value =  DateFormatUtils.ISO_DATETIME_FORMAT.format(RandomUtil.date());
                    } else
                    if(lowerCase.contains("lat")){
                        String randomLongLat = RandomUtil.randomLongLat(25, 115, 26, 160);
                        value = StringUtils.split(randomLongLat,",")[1];
                    }else
                    if(lowerCase.contains("lon") || lowerCase.contains("lang")){
                        String randomLongLat = RandomUtil.randomLongLat(25, 115, 26, 160);
                        value = StringUtils.split(randomLongLat,",")[0];
                    }else if(lowerCase.contains("pic") || lowerCase.contains("photo")){
                        value = RandomUtil.photoURL();
                    }else if(lowerCase.contains("num")){
                        value = RandomUtils.nextInt(10) + "";
                    }else if(lowerCase.contains("url")){
                        value = "http://www.baidu.com";
                    }
                    else if(lowerCase.contains("no") || lowerCase.contains("code")){
                        value = RandomStringUtils.randomNumeric(4);
                    }

                    ReflectionUtils.invokeMethod(writeMethod,object,value);
                }else if(propertyType == Date.class){
                    ReflectionUtils.invokeMethod(writeMethod,object, RandomUtil.date());
                }else if(propertyType == BigDecimal.class){
                    ReflectionUtils.invokeMethod(writeMethod,object, new BigDecimal(RandomUtils.nextDouble()));
                }else if (propertyType == Integer.class || propertyType == int.class || propertyType == Long.class ||  propertyType == long.class){
                    if(columnName.contains("age")){
                        ReflectionUtils.invokeMethod(writeMethod,object, RandomUtils.nextInt(150));
                    }else {
                        ReflectionUtils.invokeMethod(writeMethod, object, RandomUtils.nextInt());
                    }
                }else if(propertyType == Float.class || propertyType == float.class || propertyType == Double.class || propertyType == double.class){
                    ReflectionUtils.invokeMethod(writeMethod,object, RandomUtils.nextFloat());
                }else if(propertyType == Boolean.class || propertyType == boolean.class){
                    ReflectionUtils.invokeMethod(writeMethod,object, RandomUtils.nextBoolean());
                }
            }else{
                // 除了原始类型，其它就是复杂类型
                Object columnValue = populateDataComplex(propertyType);
                ReflectionUtils.invokeMethod(writeMethod,object,columnValue);
            }
        }
        return object;
    }
}
