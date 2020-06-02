package learntest.apache.commons;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class StringUtilsTest {
    @Test
    public void leftPad(){
        String s = StringUtils.leftPad("1", 3, '0');
        System.out.println(s);
    }

    @Test
    public void testSplit(){
        String s = "1$2$$";
        System.out.println(s.split("\\$").length);
        System.out.println(s.split("\\$",3).length);
        System.out.println(StringUtils.split(s,"\\$",3).length);
    }

    /**
     * 文本相似度计算
     * 版本太低,不支持
     */
//    public static void similarity() {
//        JaccardSimilarity jaccardSimilarity = new JaccardSimilarity();
//        String str1 = "网站拒绝重复写信功能，";
//        String str2 = "网站拒绝重复写信功能";
//        double value = jaccardSimilarity.apply(str1, str2);
//        System.out.println("相似度=" + value + "\n");
//    }

//    /**
//     * 随机字符串生成
    // 当前 版本太低,不支持
//     */
//    public static void generateRandomString(){
//        RandomStringGenerator.Builder builder = new RandomStringGenerator.Builder();
//        RandomStringGenerator generator = null;
//        String s = "";
//
//        //使用字母 a-z
//        generator = builder.withinRange('a', 'z').build();
//        s = generator.generate(20);
//        System.out.println(StringUtils.center("随机字母字符串", 20, "="));
//        System.out.println(s);
//
//        //使用数字 0-9
//        generator = builder.withinRange('0', '9').build();
//        s = generator.generate(20);
//        System.out.println(StringUtils.center("随机数字字符串", 20, "="));
//        System.out.println(s);
//
//        //使用字符 0-z
//        generator = builder.withinRange('0', 'z').build();
//        s = generator.generate(20);
//        System.out.println(StringUtils.center("随机混合字符串", 20, "="));
//        System.out.println(s + "\n");
//    }

    /**
     * 随机字符串
     */
    public static void randomString(){
        System.out.println(RandomStringUtils.randomAlphabetic(5)); //大小写字母组合
        System.out.println(RandomStringUtils.randomAlphanumeric(5)); //大小写字母、数字的组合
        System.out.println(RandomStringUtils.randomAscii(5)); //Ascii码
        System.out.println(RandomStringUtils.randomNumeric(5)); //随机数字
    }

    /**
     * 占位符替换
     */
    public static void strSubstitutor(){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user", "admin");
        params.put("password", "123456");
        params.put("system-version", "windows 10");
        params.put("版本", "version");

        //StrSubstitutor不是线程安全的类
        StrSubstitutor strSubstitutor = new StrSubstitutor(params, "${", "}");
        //是否在变量名称中进行替换
        strSubstitutor.setEnableSubstitutionInVariables(true);
        String s = strSubstitutor.replace("你的用户名是${user},密码是${password}。系统版本${system-${版本}}");
        System.out.println(s);
    }
}
