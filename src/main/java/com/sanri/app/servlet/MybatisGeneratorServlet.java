package com.sanri.app.servlet;

import com.sanri.app.BaseServlet;
import com.sanri.app.postman.CodeGeneratorConfig;
import com.sanri.frame.RequestMapping;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.util.EntityUtils;
import org.dom4j.*;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.util.ReflectionUtils;
import sanri.utils.HttpUtil;
import sanri.utils.VelocityUtil;
import sanri.utils.ZipUtil;

import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

@RequestMapping("/mybatis/code")
public class MybatisGeneratorServlet extends BaseServlet {
    private static File mybatisCode ;
    private static File projectCode;
    static {
        mybatisCode = mkTmpPath("/mybatisCode");
        projectCode = mkTmpPath("/projectCode");
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
        String mbgXmlConfig = VelocityUtil.formatFile("/com/sanri/config/templates/springboot/tkmapper.xml", configs);

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

       return targetDir.getName();
    }

    /**
     * 直接生成完整项目
     * @param codeGeneratorConfig
     * @return
     */
    public String projectBuild(CodeGeneratorConfig codeGeneratorConfig) throws IOException, XMLParserException, InvalidConfigurationException, SQLException, InterruptedException, DocumentException {
        //创建项目存放目录
        codeGeneratorConfig.getConnectionConfig().config();
        File targetDir = new File(projectCode, System.currentTimeMillis() + "");
        targetDir.mkdir();
        codeGeneratorConfig.setFilePath(targetDir.getAbsolutePath());

        // 生成 maven 骨架,调用 springboot 官网生成，引用 web-ui excel-poi 后面做成可配置
        Map<String,String> params = new LinkedHashMap<>();
        params.put("type","maven-project");
        params.put("language","java");
        params.put("bootVersion",codeGeneratorConfig.getMavenConfig().getSpringBootVersion());
        params.put("baseDir",codeGeneratorConfig.getProjectName());
        params.put("groupId",codeGeneratorConfig.getMavenConfig().getGroupId());
        params.put("artifactId",codeGeneratorConfig.getMavenConfig().getArtifactId());
        params.put("name",codeGeneratorConfig.getProjectName());
        params.put("description","");
        params.put("packageName",codeGeneratorConfig.getPackageConfig().getBase());
        params.put("packaging","jar");
        params.put("javaVersion","1.8");
        List<NameValuePair> nameValuePairs = HttpUtil.transferParam(params);
        HttpEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairs, Consts.UTF_8);
        String keyValueParams = EntityUtils.toString(urlEncodedFormEntity ,Consts.UTF_8);
        InputStream inputStream = HttpUtil.getStream("https://start.spring.io/starter.zip", keyValueParams);
        File springProjectFile = new File(targetDir, codeGeneratorConfig.getProjectName()+".zip");
        FileOutputStream fileOutputStream = new FileOutputStream(springProjectFile);
        IOUtils.copy(inputStream,fileOutputStream);
        IOUtils.closeQuietly(fileOutputStream);IOUtils.closeQuietly(inputStream);
        //解压文件
        ZipUtil.unzip(springProjectFile,targetDir.getAbsolutePath());
        //删除压缩包
        FileUtils.forceDelete(springProjectFile);

        //开始配置项目
        File projectDir = new File(targetDir, codeGeneratorConfig.getProjectName());

        //使用 tk.mybatis 生成单表结构数据
        Map<String,Object> configs = new HashMap<>();
        // 设置生成目录为项目目录
        File javaDir = new File(projectDir,"src/main/java");
        codeGeneratorConfig.setFilePath(javaDir.getAbsolutePath());
        configs.put("config",codeGeneratorConfig);
        configs.put("StringUtils",StringUtils.class);
        configs.put("ArrayUtils", ArrayUtils.class);
        String mbgXmlConfig = VelocityUtil.formatFile("/com/sanri/config/templates/springboot/tkmapper.xml", configs);

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

        //配置数据源,项目名，端口号
        File resourcesDir = new File(projectDir, "/src/main/resources");
        File application = new File(resourcesDir, "application.properties");
        String formatFile = VelocityUtil.formatFile("/com/sanri/config/templates/springboot/application.properties", configs);
        FileUtils.writeStringToFile(application,formatFile);

        //添加一个文件来消除 mybatis 警告
        String basePackagePath = StringUtils.replace(codeGeneratorConfig.getPackageConfig().getBase(), ".", "/");
        File basePackage = new File(javaDir,basePackagePath );basePackage.mkdirs();
        File NoWarnMapper = new File(basePackage, "NoWarnMapper.java");
        String NoWarnMapperContent = VelocityUtil.formatFile("/com/sanri/config/templates/springboot/NoWarnMapper.java", configs);
        FileUtils.writeStringToFile(NoWarnMapper,NoWarnMapperContent);

        // 覆盖原来的 pom 文件
        File pomFile = new File(projectDir, "pom.xml");
        String pomContent = VelocityUtil.formatFile("/com/sanri/config/templates/springboot/pom.xml", configs);
        FileUtils.writeStringToFile(pomFile,pomContent);

        // 生成 service,controller,vo,dto,param
        mkdirs(javaDir,codeGeneratorConfig.getPackageConfig());

        return targetDir.getName()+"/"+codeGeneratorConfig.getProjectName();
    }

    private void mkdirs(File javaDir, CodeGeneratorConfig.PackageConfig packageConfig) {
        PropertyDescriptor[] beanGetters = ReflectUtils.getBeanGetters(CodeGeneratorConfig.PackageConfig.class);
        for (int i = 0; i < beanGetters.length; i++) {
            Method readMethod = beanGetters[i].getReadMethod();
            String path = Objects.toString(ReflectionUtils.invokeMethod(readMethod, packageConfig));
            String[] split = StringUtils.split(path, '.');
            StringBuffer currentPath = new StringBuffer();
            for (String partPath : split) {
                currentPath.append("/").append(partPath);
                File dir = new File(javaDir, currentPath.toString());
                if(!dir.exists()){
                    logger.info("创建目录 :"+dir);
                    dir.mkdir();
                }
            }
        }
    }
}
