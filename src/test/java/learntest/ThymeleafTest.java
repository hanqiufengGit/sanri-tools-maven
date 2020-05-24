package learntest;

import com.alibaba.fastjson.JSONObject;
import io.swagger.models.Swagger;
import org.junit.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import sanri.utils.HttpUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ThymeleafTest {

    @Test
    public void testToWord(){
        // 创建模板解析器，用来加载模板
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setPrefix("com/sanri/config/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("utf-8");

        // 创建模板引擎
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        Context context = new Context();
        Map<String,String> data = new HashMap<>();
        data.put("welcome","试试中文不乱码sanri1993");
        context.setVariables(data);
        String process = templateEngine.process("apis", context);

        System.out.println(process);
    }

    @Test
    public void testSwagger() throws IOException {
        String json =  HttpUtil.getData("http://localhost:8080/v2/api-docs",null);
        Swagger swagger = JSONObject.parseObject(json, Swagger.class);
        Map<String,Object> data = new HashMap<>();
        data.put("doc",swagger);

        // 创建模板解析器，用来加载模板
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setPrefix("com/sanri/config/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("utf-8");

        // 创建模板引擎
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        Context context = new Context();
        context.setVariables(data);
        String process = templateEngine.process("apis", context);
        System.out.println(process);
    }
}
