package minitest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sanri.app.dtos.kafka.BrokerTopicMetrics;
import com.sanri.app.jdbc.ExConnection;
import com.sanri.app.jdbc.Table;
import com.sanri.app.jdbc.codegenerate.SimpleJavaBeanBuilder;
import com.sanri.app.translate.TranslateCharSequence;
import com.sanri.app.translate.TranslateSupport;
import com.sanri.initexec.InitJdbcConnections;
import freemarker.template.Version;
import io.swagger.models.Swagger;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.core.Constants;
import org.springframework.util.ReflectionUtils;
import sanri.utils.HttpUtil;
import sanri.utils.PathUtil;
import sanri.utils.RandomUtil;
import sanri.utils.URLUtil;
import sanri.utils.regex.RegexValidate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

public class ToolsSysTest {

    /**
     * 用正则来解析 java 中的类名,包名,字段名
     */
    Pattern packageNamePattern =  Pattern.compile("package (.*);");
    Pattern classNamePattern = Pattern.compile("public\\s+class\\s+(.*)\\{");
    Pattern fieldPattern = Pattern.compile("(private\\s\\w+\\s\\w+)",Pattern.MULTILINE & Pattern.LITERAL);
    @Test
    public void testRegexParse() throws IOException {
        String content = FileUtils.readFileToString(new File("d:/test/FileInfo.java"));
        String packageName = RegexValidate.match(content, this.packageNamePattern).get(0);
        String className = RegexValidate.match(content, this.classNamePattern).get(0);
        List<String> match = RegexValidate.match(content, fieldPattern);
        System.out.println(packageName);
        System.out.println(className);
        System.out.println(match);

    }
//    @Test
//    public void generateJavaBean(){
//        JavaBean javaBean = new JavaBean();
//        javaBean.setClassName("HelloWord");
//        javaBean.setClassComment("@table hello_word");
//        javaBean.setPackageName("com.sanri");
//        Map<String,String> propertys = new HashMap<String, String>();
//        Map<String,String> propertysComments = new HashMap<String, String>();
//        javaBean.setPropertysComments(propertysComments);
//        javaBean.setPropertys(propertys);
//
//        propertys.put("username", "String");
//        propertys.put("age", "int");
//        propertys.put("time", "java.util.Date");
//        propertysComments.put("username", "用户名,可重复");
//        propertysComments.put("time", "格式 yyyy-MM-dd");
//
//        List<String> build = javaBean.build();
//		File writerBean = javaBean.writerBean(build, "d:/abcd");
//        System.out.println(build);
//
//    }

    @Test
    public void testBuildSimpleJavaBean(){
        List<SimpleJavaBeanBuilder.Property> properties = new ArrayList<>();
        properties.add(new SimpleJavaBeanBuilder.Property("String","name"));
        properties.add(new SimpleJavaBeanBuilder.Property("int","age"));
        SimpleJavaBeanBuilder simpleJavaBeanBuilder = new SimpleJavaBeanBuilder("com.sanri.app.dtos","TransferDto",properties);
        List<String> build = simpleJavaBeanBuilder.build();
        System.out.println(StringUtils.join(build,'\n'));
    }

    @Test
    public void swagger() throws IOException {
        String data =  HttpUtil.getData("http://localhost:8080/v2/api-docs",null);
        Swagger swagger = JSONObject.parseObject(data, Swagger.class);
        System.out.println(swagger);
    }
    @Test
    public void test2() throws IOException {
        String data = FileUtils.readFileToString(new File("d:/logs/pert.json"));
        Swagger swagger = JSONObject.parseObject(data, Swagger.class);
        System.out.println(swagger);
    }
    @Test
    public void testVersion(){
        Version version = new Version("1.4.0");
        System.out.println(version.getMajor());
        System.out.println(version.getMinor());
        System.out.println(version.getMicro());

        System.out.println(version.intValue());
    }

    @Test
    public void testURI() throws URISyntaxException, MalformedURLException {
        URI uri = new URI("ftp://ftpadmin:salt202@10.101.70.202:21//scp-st-informationreleaseapp/20190917/1568705443741.txt");
        String host = uri.getHost();
        URI pathURI = new URI(uri.getPath());
//        URI relativize = pathURI.relativize(new URI(".."));
        URI relativize = new URI("../").relativize(pathURI);

        System.out.println(relativize);
        URL url = new URL("http", host, relativize.toString());
        System.out.println(url);

        String path = "/scp-st-informationreleaseapp/20190917/1568705443741.txt";
//        URI relativize = new URI("/scp-st-informationreleaseapp").relativize(new URI(path));
//        System.out.println(new URL("https","192.168.1.1","/"+relativize.getPath()));

    }

    @Test
    public void testTranslate(){
        TranslateCharSequence translateCharSequence = new TranslateCharSequence("我是中国人");
        translateCharSequence.addSegment(Arrays.asList("我","是","中国人"));
        translateCharSequence.addSegment(Arrays.asList("我","是","中国","人"));

        translateCharSequence.addTranslate("我","I");
        translateCharSequence.addTranslate("是","am");
        translateCharSequence.addTranslate("是","is");
        translateCharSequence.addTranslate("中国","china");
        translateCharSequence.addTranslate("人","humman");
        translateCharSequence.addTranslate("人","person");
        translateCharSequence.addTranslate("人","db");
        translateCharSequence.addTranslate("中国人","chinaman");

        Set<String> results = translateCharSequence.results();
        System.out.println(results);
    }

    @Test
    public void testClasses() throws NoSuchFieldException {
        Field b = A.class.getDeclaredField("b");
        Class<?> type = b.getType();
        System.out.println(type == Collection.class);
        System.out.println(type == List.class);

        Field c = A.class.getDeclaredField("c");
        Class<?> type1 = c.getType();
        boolean array = type1.isArray();
        if(array){
            Class<?> componentType = type1.getComponentType();
        }
    }

    @Test
    public void testClass() throws NoSuchFieldException {

        JSONObject jsonObject = JSON.parseObject("{\"playSpan\":[{\"beginTime\":\"20200323T200314+08\",\"endTime\":\"20200323T210314+08\",\"id\":1,\"programNo\":401}]}");
        // 日
        boolean b = checkDay(jsonObject);
        System.out.println(b);

//        // 周
//        Calendar calendar = Calendar.getInstance();
//        int current = calendar.get(Calendar.DAY_OF_WEEK);
//        JSONArray dayList = jsonObject.getJSONArray("dayList");
//        for (int i = 0; i < dayList.size(); i++) {
//            JSONObject partDay = dayList.getJSONObject(i);
//            String dayOfWeek = partDay.getString("dayOfWeek");
//            int dayOfWeekInt = parser(dayOfWeek);
//            if(current > dayOfWeekInt){
//                JSONArray dailySchedule = partDay.getJSONArray("dailySchedule");
//                boolean b1 = checkPlaySpan(dailySchedule);
//                if(b1)return true;
//            }
//        }
//        return false;
    }


    private boolean checkDay(JSONObject jsonObject) {
        JSONArray playSpan = jsonObject.getJSONArray("playSpan");
        if (checkPlaySpan(playSpan)) return true;
        return false;
    }
    public static final String TIME_FORMAT_UTC = "yyyyMMdd'T'HHmmss'+08'";
    private boolean checkPlaySpan(JSONArray playSpan) {
        int size = playSpan.size();
        for (int i = 0; i < size; i++) {
            JSONObject part = playSpan.getJSONObject(i);
            String endTime = part.getString("endTime");
            try {
                Date parseDate = DateUtils.parseDate(endTime,TIME_FORMAT_UTC);
                System.out.println(parseDate);
                boolean before = parseDate.after(new Date());
                if(before){
                    return true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public class A{
        private List<String> b = new ArrayList<>();
        private String [] c = null;
    }

    @Test
    public void testUnzip(){
//		unzip(new File("D:\\tmp\\projectCode\\1572417685251/cardtest.zip"),"D:\\tmp\\projectCode\\1572417685251");
        int a = 0;
        String b = a+"b";
        System.out.println(b);
    }

    @Test
    public void testsh(){
        int n=1;
        label:while (n++ <= 50 ){
            for (int i = 2; i <n ; i++) {
                if(n % i == 0){
                    continue label;
                }
            }
            System.out.print(" "+n+" ");
        }
    }

    public static final char [] punctuations = {',','!','-','.'};

    @Test
    public void testPun(){
        String origin = "drop-addValue-addedServicePlatform";
        for (char punctuation : punctuations) {
            origin = origin.replace(punctuation,' ');
        }
        String convert2aB = TranslateSupport.convert2aB(origin);
        System.out.println(convert2aB);
    }


    @Test
    public void testAsm() throws IOException {
        InputStream resourceAsStream = ToolsSysTest.class.getResourceAsStream("/com/sanri/app/chat/ChatMessage.class");
        ClassReader reader = new ClassReader(resourceAsStream);
        ClassNode cn = new ClassNode();//创建ClassNode,读取的信息会封装到这个类里面
        reader.accept(cn, 0);//开始读取

        String name = cn.name;
        System.out.println(name);
        resourceAsStream.close();
    }

    @Test
    public void testLoadTables() throws SQLException {
        InitJdbcConnections initJdbcConnections = new InitJdbcConnections();
        initJdbcConnections.execute();
        ExConnection exConnection = InitJdbcConnections.CONNECTIONS.get("hdsc");
        List<Table> tables = exConnection.tables("hdsc_db", true);
        for (Table table : tables) {
            String tableName = table.getTableName();
            if(tableName.startsWith("mct")){
                System.out.println("-- 删除表 "+table.getComments());
                System.out.println("truncate "+ tableName +";");
            }
        }

    }

    @Test
    public void testBaseName() throws URISyntaxException {
        String str = "src/main/java/sanri/utils/MailUtil.java";
        System.out.println(FilenameUtils.getBaseName(str));
        System.out.println(URLUtil.pathLast(str));
    }

    @Test
    public void testPath(){
        File projectDir = new File(PathUtil.ROOT.resolve("../../"));
        System.out.println(projectDir);
        System.out.println(PathUtil.ROOT);
    }

    @Test
    public void testRandom(){
        for (int i = 0; i < 10000; i++) {
            System.out.println(RandomUtil.phone());
            System.out.println(RandomUtil.username());
            System.out.println(RandomUtil.address());
            String idcard = RandomUtil.idcard();
            System.out.println(idcard);
            if(idcard.endsWith("x") || idcard.endsWith("X")){
                System.out.println("--------------");
            }
            System.out.println(RandomUtil.job());
            System.out.println(RandomUtil.email(30));
            System.out.println(RandomUtil.timstamp());
            System.out.println(DateFormatUtils.ISO_DATETIME_FORMAT.format(RandomUtil.date()));
        }
    }

    @Test
    public void test() throws NoSuchMethodException {
        Constants constants = new Constants(BrokerTopicMetrics.BrokerMetrics.class);
        Method getFieldCache = ReflectUtils.findDeclaredMethod(Constants.class, "getFieldCache", null);
        getFieldCache.setAccessible(true);
        Map<String, Object> invokeMethod = (Map<String, Object>) ReflectionUtils.invokeMethod(getFieldCache, constants);
        System.out.println(invokeMethod);
    }

    @Test
    public void testTruncate(){
        String random = RandomStringUtils.randomAlphabetic(21);
        System.out.println(random);
        String truncate = truncate(random);
        System.out.println(truncate);
    }

    public static String truncate(String q) {
        if (q == null) {
            return null;
        }
        int len = q.length();
        String result;
        return len <= 20 ? q : (q.substring(0, 10) + len + q.substring(len - 10, len));
    }

    @Test
    public void testxxx(){
        List<String> strings = JSONArray.parseArray(JSON.toJSONString(null), String.class);

    }
}
