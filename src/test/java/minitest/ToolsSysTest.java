package minitest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sanri.app.translate.TranslateCharSequence;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.*;

public class ToolsSysTest {
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
}
