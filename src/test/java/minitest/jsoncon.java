package minitest;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringEscapeUtils;

public class jsoncon {
    public static void main(String[] args) {
        b m = new b("m");
        a a = new a("a", m);

        String s = JSONObject.toJSONString(a);
        String s1 = StringEscapeUtils.escapeJson(s);
        System.out.println(s1);
    }

    static class b{
        private String c;

        public b(String c) {
            this.c = c;
        }

        public String getC() {
            return c;
        }
    }
   static class a{
        private String a ;
        private b c;

        public a(String a, b c) {
            this.a = a;
            this.c = c;
        }

       public String getA() {
           return a;
       }

       public b getC() {
           return c;
       }
   }
}
