package com.sanri.app.jsoup;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.text.StringSubstitutor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SpiderHelper {
    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36";
    private Logger log = LoggerFactory.getLogger(SpiderHelper.class);

    public Object populate(Class clazz,Element element){
        Object object = ReflectUtils.newInstance(clazz);

        Class currentClass = clazz;
        while(currentClass != Object.class){
            try {
                Field[] declaredFields = currentClass.getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    Select select = AnnotationUtils.getAnnotation(declaredField, Select.class);
                    if(select == null)continue;

                    String cssQuery = select.value();
                    if(StringUtils.isBlank(cssQuery))continue;

                    Elements elements = element.select(cssQuery);
                    if(elements != null && elements.size() > 0){
                        Element el = elements.get(0);

                        Class<?> type = declaredField.getType();
                        if(type == List.class){
                            List list = new ArrayList();
                            ParameterizedType genericType = (ParameterizedType)declaredField.getGenericType();
                            Class listType = (Class) genericType.getActualTypeArguments()[0];
                            Select listTypeSelect = AnnotationUtils.getAnnotation(listType, Select.class);
                            Elements listEls = el.select(listTypeSelect.value());
                            Iterator<Element> iterator = listEls.iterator();
                            while (iterator.hasNext()){
                                Element currEl = iterator.next();
                                // 注入 List 对象
                                Object populate = populate(listType, currEl);
                                list.add(populate);
                            }

                            FieldUtils.writeField(declaredField,object,list,true);
                            continue;
                        }

                        String attr = select.attr();

                        String value = "";
                        if("content".equalsIgnoreCase(attr)){
                            value = el.text();
                        }else{
                            value = el.attr(attr);
                        }

                        FieldUtils.writeField(declaredField,object,value,true);
                    }
                }
            }catch (Exception e){
                log.error("反射异常[{}]",e.getMessage(),e);
            }finally {
                currentClass = clazz.getSuperclass();
            }
        }
        return object;
    }

    public Object spider(Class clazz, Map<String,String> params) throws IOException {
        StringSubstitutor stringSubstitutor = new StringSubstitutor(params, "${", "}");

        Request request = AnnotationUtils.getAnnotation(clazz, Request.class);
        String address = stringSubstitutor.replace(request.value());
        Document document = Jsoup.connect(address)
                .userAgent(userAgent)
                .validateTLSCertificates(false)
                .timeout(10000).get();

        return populate(clazz,document);
    }
}
