package com.sanri.app.servlet;

import com.sanri.app.BaseServlet;
import com.sanri.app.jdbc.ExConnection;
import com.sanri.app.jdbc.Table;
import com.sanri.app.postman.CodeGeneratorConfig;
import com.sanri.frame.RequestMapping;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;
import sanri.utils.VelocityUtil;
import sanri.utils.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/mybatis/code")
public class MybatisGeneratorServlet extends BaseServlet {
    private static File mybatisCode ;
    static {
        mybatisCode = mkTmpPath("/mybatisCode");
    }
    /**
     * 多表或单表部分代码生成,使用 mbg
     * @param codeGeneratorConfig
     * @return
     * @throws SQLException
     */
    public String tablesCode(CodeGeneratorConfig codeGeneratorConfig) throws SQLException, IOException, XMLParserException, InvalidConfigurationException, InterruptedException {
        codeGeneratorConfig.getConnectionConfig().config();
        File targetDir = new File(mybatisCode, System.currentTimeMillis() + "");
        targetDir.mkdir();
        codeGeneratorConfig.setFilePath(targetDir.getAbsolutePath());

        Map<String,Object> configs = new HashMap<>();
        configs.put("config",codeGeneratorConfig);
        configs.put("StringUtils",StringUtils.class);
        configs.put("ArrayUtils", ArrayUtils.class);
        String mbgXmlConfig = VelocityUtil.formatFile("/com/sanri/config/tkmapper.xml", configs);

        List<String> warnings = new ArrayList<String>();
        boolean overwrite = true;
        ConfigurationParser cp = new ConfigurationParser(warnings);
        System.out.println(mbgXmlConfig);
        Configuration config  = cp.parseConfiguration(new StringReader(mbgXmlConfig));
        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
        for (String warning : warnings) {
            System.out.println(warning);
        }

        File zipFile = ZipUtil.zip(targetDir);
        return zipFile.getName();
    }

    public String projectBuild(CodeGeneratorConfig codeGeneratorConfig){

        return "";
    }
}
