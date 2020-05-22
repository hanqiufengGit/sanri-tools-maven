package com.sanri.app.classloader;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.util.ReflectionUtils;
import sanri.utils.RandomUtil;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.*;

/**
 * 随机数据生成
 */
public class RandomDataService {
    private Logger logger = LoggerFactory.getLogger(RandomDataService.class);

    public ClassLoaderManager classLoaderManager = ClassLoaderManager.getInstance();

    public Object randomData(String className,ClassLoader classLoader) throws ClassNotFoundException {
        Class<?> clazz = classLoader.loadClass(className);
        return populateDataComplex(clazz);
    }

    public Object randomData(String className,String classloaderName) throws ClassNotFoundException {
        ExtendClassloader extendClassloader = classLoaderManager.get(classloaderName);
        return randomData(className,extendClassloader);
    }

    /**
     * 对象注入复杂类型数据
     * @param clazz
     * @return
     */
    public Object populateDataComplex(Class<?> clazz) {
        Object object = ReflectUtils.newInstance(clazz);

        PropertyDescriptor[] beanSetters = ReflectUtils.getBeanSetters(clazz);
        for (PropertyDescriptor beanSetter : beanSetters) {
            Method writeMethod = beanSetter.getWriteMethod();
            String columnName = beanSetter.getName();
            Class<?> propertyType = beanSetter.getPropertyType();
            if(isPrimitiveExtend(propertyType)){
                Object value = populateDataOrigin(columnName, propertyType);
                ReflectionUtils.invokeMethod(writeMethod,object,value);
            }else{
                // 除了原始类型，其它就是复杂类型 Collection Map Array 或其它复杂对象
                if(propertyType == List.class){
                    List list = new ArrayList();

                    populateCollectionData(writeMethod, columnName, list);
                    // 注入 list 数据
                    ReflectionUtils.invokeMethod(writeMethod,object,list);
                }else if(propertyType == Set.class){
                    Set set = new HashSet();
                    populateCollectionData(writeMethod,columnName,set);
                    // 注入 set 数据
                    ReflectionUtils.invokeMethod(writeMethod,object,set);
                }else if(propertyType == Map.class){
                    Map map = new HashMap();

                    ParameterizedType parameterType = (ParameterizedType)writeMethod.getGenericParameterTypes()[0];

                    Class keyTypeArgument = (Class) parameterType.getActualTypeArguments()[0];
                    Class valueTypeArgument = (Class) parameterType.getActualTypeArguments()[1];

                    for (int i = 0; i < 10; i++) {
                        Object key = null,value = null;
                        if(isPrimitiveExtend(keyTypeArgument)){
                            key = populateDataOrigin(columnName,keyTypeArgument);
                        }else{
                            key = populateDataComplex(keyTypeArgument);
                        }

                        if(isPrimitiveExtend(valueTypeArgument)){
                            value = populateDataOrigin(columnName,valueTypeArgument);
                        }else{
                            value = populateDataComplex(valueTypeArgument);
                        }

                        map.put(key,value);
                    }
                }else {
                    if (propertyType.isArray()){
                        Object [] array = new Object[10];
                        Class<?> componentType = propertyType.getComponentType();
                        if(isPrimitiveExtend(componentType)){
                            for (int i = 0; i < 10; i++) {
                                array[i] = populateDataOrigin(columnName,componentType);
                            }
                        }else{
                            for (int i = 0; i < 10; i++) {
                                array [i] = populateDataComplex(componentType);
                            }
                        }

                        ReflectionUtils.invokeMethod(writeMethod, object, array);
                        continue;
                    }
                    String packageName = propertyType.getPackage().getName();
                    if(packageName.startsWith("java.*")){
                        logger.error("当前类型[{}]无法注入数据,来自包[{}]",propertyType,packageName);
                        continue;
                    }
                    Object columnValue = populateDataComplex(propertyType);
                    ReflectionUtils.invokeMethod(writeMethod, object, columnValue);
                }
            }
        }
        return object;
    }

    private void populateCollectionData(Method writeMethod, String columnName, Collection list) {
        ParameterizedType genericParameterType = (ParameterizedType)writeMethod.getGenericParameterTypes()[0];
        Class typeArgument = (Class) genericParameterType.getActualTypeArguments()[0];
        if(isPrimitiveExtend(typeArgument)){
            for (int i = 0; i < 10; i++) {
                Object dataOriginType = populateDataOrigin(columnName,typeArgument);
                list.add(dataOriginType);
            }
        }else{
            // 每个 List 创建 10 条记录
            for (int i = 0; i < 10; i++) {
                Object dataComplex = populateDataComplex(typeArgument);
                list.add(dataComplex);
            }
        }
    }

    /**
     * 判断是否是原始型扩展
     * 包含 原始型及包装类,String,Date,BigDecimal
     * @param propertyType
     * @return
     */
    private boolean isPrimitiveExtend(Class<?> propertyType) {
        return ClassUtils.isPrimitiveOrWrapper(propertyType) || propertyType == String.class || propertyType == Date.class || propertyType == BigDecimal.class;
    }

    /**
     * 对某一列注入原始类型的数据
     * @param columnName
     * @param propertyType
     * @return
     */
    private Object populateDataOrigin( String columnName, Class<?> propertyType) {
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

           return value;
        }
        if(propertyType == Date.class){
            return RandomUtil.date();
        }
        if(propertyType == BigDecimal.class){
            return new BigDecimal(RandomUtils.nextDouble());
        }
        if (propertyType == Integer.class || propertyType == int.class || propertyType == Long.class ||  propertyType == long.class){
            Integer num = RandomUtils.nextInt();
            if(columnName.contains("age")) {
                num = RandomUtils.nextInt(150);
            }
            return num;
        }else if(propertyType == Float.class || propertyType == float.class || propertyType == Double.class || propertyType == double.class){
            return RandomUtils.nextFloat();
        }else if(propertyType == Boolean.class || propertyType == boolean.class){
            return RandomUtils.nextBoolean();
        }
        return null;
    }
}
