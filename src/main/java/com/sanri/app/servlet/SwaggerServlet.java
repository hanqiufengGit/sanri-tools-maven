package com.sanri.app.servlet;

import com.sanri.app.BaseServlet;
import com.sanri.app.swagger.Doc;
import com.sanri.app.swagger.SwaggerJsonParser;
import com.sanri.frame.ContextLoaderListener;
import com.sanri.frame.RequestMapping;
import com.sanri.initexec.ThymeleafInit;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.thymeleaf.context.Context;
import sanri.utils.VelocityUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/swagger")
public class SwaggerServlet extends BaseServlet {
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

    /**
     * 生成 word 文档
     * @param url
     * @return
     */
    public void word(String url, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String html = html(url);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(html.getBytes("utf-8"));
        String fileName = "接口文档"+ DateFormatUtils.ISO_DATETIME_FORMAT.format(new Date());
        download(byteArrayInputStream,MimeType.WORD,fileName,request,response);
    }

    /**
     * 生成 markdown
     * @param url
     * @param request
     * @param response
     * @throws IOException
     */
    public void markdown(String url, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Doc doc = swaggerJsonParser.doc(url);
        Map<String,Object> data = new HashMap<>();
        data.put("doc",doc);
        data.put("swaggerURL",url);
        data.put("h1","#");data.put("h2","##");data.put("h3","###");
        String markdown = VelocityUtil.formatFile("/com/sanri/config/templates/markdown.tpl", data);
        String fileName = "接口文档"+ DateFormatUtils.ISO_DATETIME_FORMAT.format(new Date());
        download(new ByteArrayInputStream(markdown.getBytes("utf-8")),MimeType.MARKDOWN,fileName,request,response);
    }
}
