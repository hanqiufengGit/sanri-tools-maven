package com.sanri.app.apidoc;

import com.alibaba.fastjson.JSONObject;
import com.sanri.app.servlet.FileManagerServlet;
import com.sanri.frame.DispatchServlet;
import io.swagger.models.Swagger;
import sanri.utils.HttpUtil;

import java.io.IOException;

public class SwaggerUIDocService {
    private static String module= "swaggerui";
    private FileManagerServlet fileManagerServlet = DispatchServlet.getServlet(FileManagerServlet.class);

    /**
     * 获取 swagger 对象
     * @param connName 连接名
     * @return
     */
    public Swagger swagger(String connName) throws IOException {
        String swaggerJsonURL = fileManagerServlet.readConfig(module, connName);
        String data =  HttpUtil.getData(swaggerJsonURL,null);
        Swagger swagger = JSONObject.parseObject(data, Swagger.class);
        return swagger;
    }
}
