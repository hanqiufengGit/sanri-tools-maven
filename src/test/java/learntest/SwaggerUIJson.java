package learntest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.models.Swagger;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import sanri.utils.HttpUtil;

import java.io.File;
import java.io.IOException;

public class SwaggerUIJson {
    public static void main(String[] args) throws IOException {
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
}
