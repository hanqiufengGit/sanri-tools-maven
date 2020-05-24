package com.sanri.app.servlet;

import com.sanri.app.swagger.Doc;
import com.sanri.app.swagger.SwaggerJsonParser;
import com.sanri.frame.ContextLoaderListener;
import com.sanri.frame.RequestMapping;
import com.sanri.initexec.ThymeleafInit;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/swagger")
public class SwaggerServlet {
    SwaggerJsonParser swaggerJsonParser  = new SwaggerJsonParser();

    /**
     * 生成给前端 json 数据
     * @param url
     * @return
     * @throws IOException
     */
    public Doc doc(String url) throws IOException{
        return swaggerJsonParser.doc(url);
    }

    /**
     * 使用模板引擎生成 html 界面给前端
     * @return
     */
    public String html(String url) throws IOException {
        Doc doc = swaggerJsonParser.doc(url);
        Map<String,Object> docMap = new HashMap<>();
        docMap.put("doc",doc);

        ThymeleafInit thymeleafInit = (ThymeleafInit)ContextLoaderListener.getInitInstance(ThymeleafInit.class);
        Context context = new Context();
        context.setVariables(docMap);
        String word = thymeleafInit.templateEngine.process("word", context);
        return word;
    }
}
