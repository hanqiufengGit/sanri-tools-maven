package learntest;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.Test;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.List;

public class ReflectUtilsTest {

    @Test
    public void test() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        // MethodUtil 主要是调用方法功能
        Object intValue = MethodUtils.invokeMethod(1, "intValue");
        System.out.println(intValue);

        //ClassUtils 主要用于获取 class
        Class<?> clazz = ClassUtils.getClass("learntest.ReflectUtilsTest");
        System.out.println(clazz);

        //这个能查不需要参数的方法
        Method test = ReflectionUtils.findMethod(clazz, "test");
        System.out.println(test);

        //主要用于获取class 信息
        PropertyDescriptor[] beanGetters = ReflectUtils.getBeanGetters(clazz);

    }

    @Test
    public void testClassReflect() throws NoSuchFieldException {
        Field users = A.class.getDeclaredField("users");
        Type gType = users.getGenericType();
        ParameterizedType pType = (ParameterizedType)gType;
        System.out.println(pType.getActualTypeArguments()[0]);
    }

    public class User {
        private String name;
    }


    public class A{
        private List<User> users;
    }
}
