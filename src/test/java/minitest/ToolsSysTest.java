package minitest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sanri.app.jsoup.netsource.PageResult;
import com.sanri.app.jsoup.netsource.PansosoSpider;
import com.sanri.app.jsoup.netsource.SourceModel;
import com.sanri.app.translate.TranslateCharSequence;
import com.sanri.app.translate.TranslateSupport;
import freemarker.template.Version;
import io.swagger.models.Swagger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import sanri.utils.HttpUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

public class ToolsSysTest {
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
    private int parser(String dayOfWeek){
        return WeekEnumeration.parserString(dayOfWeek);
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

    private PansosoSpider pansosoSpider = new PansosoSpider();
    @Test
    public void test() throws IOException {
        PageResult<SourceModel> springcloud = pansosoSpider.searchResource("springcloud", 1);
        System.out.println(springcloud);

//        Document document = Jsoup.connect("https://pan.baidu.com/s/1KX9Y_pJrLh83uS0kMKppLg")
//                .userAgent(userAgent)
//                .timeout(singlePageOpenTimeout)
//                .get();
//        Element $shareNotFound = document.getElementById("share_nofound_des");
//        if($shareNotFound != null){
//            System.out.println("链接已经失效");
//        }
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
}
