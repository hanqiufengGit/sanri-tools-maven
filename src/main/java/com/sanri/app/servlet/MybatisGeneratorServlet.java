package com.sanri.app.servlet;

import com.sanri.app.BaseServlet;
import com.sanri.app.postman.CodeGeneratorConfig;
import com.sanri.app.postman.GeneratedInfo;
import com.sanri.frame.RequestMapping;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.util.EntityUtils;
import org.dom4j.*;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
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
import tk.mybatis.mapper.generator.file.GenerateByTemplateFile;
import tk.mybatis.mapper.generator.model.TableClass;

import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
        // 从 spring 官网生成 maven 骨架
//        buildFromSpringBoot(codeGeneratorConfig, targetDir);
        //项目基本目录
        File projectDir = new File(targetDir, codeGeneratorConfig.getProjectName());

        //自己生成 maven 骨架
        File javaDir = new File(projectDir, "src/main/java");javaDir.mkdirs();
        File resourcesDir = new File(projectDir, "src/main/resources");resourcesDir.mkdirs();
        File testJavaDir = new File(projectDir, "src/test/java");testJavaDir.mkdirs();
        File testResourcesDir = new File(projectDir, "src/test/resources");testResourcesDir.mkdirs();

        //使用 tk.mybatis 生成单表结构数据
        Map<String,Object> configs = new HashMap<>();
        // 设置生成目录为项目目录
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

        // 启动类配置
        File applicationStart = new File(basePackage, StringUtils.capitalize(codeGeneratorConfig.getProjectName())+"Application.java");
        String applicationStartContent = VelocityUtil.formatFile("/com/sanri/config/templates/springboot/startApplication.java", configs);
        FileUtils.writeStringToFile(applicationStart,applicationStartContent);

        // 生成 service,controller,vo,dto,param
        mkdirs(javaDir,codeGeneratorConfig.getPackageConfig());

        //生成基本的增删改查，做为内容管理，这里只做一个基本类 service 和 controller 包含所有的增删改查
        // 内容管理 service
        String servicePackageDot = StringUtils.replace(codeGeneratorConfig.getPackageConfig().getService(),".","/");
        File servicePackage = new File(javaDir,servicePackageDot);
        Map<String,Object> serviceExtendConfigs = new LinkedHashMap<>();
        serviceExtendConfigs.putAll(configs);
        List<GeneratedJavaFile> generatedJavaFiles = myBatisGenerator.getGeneratedJavaFiles();
        GeneratedInfo generatedInfo = parserJavaFiles(generatedJavaFiles,codeGeneratorConfig);
        serviceExtendConfigs.put("generatedInfo",generatedInfo);
        String serviceContent = VelocityUtil.formatFile("/com/sanri/config/templates/springboot/ContentManagerService.java", serviceExtendConfigs);
        FileUtils.writeStringToFile(new File(servicePackage,"ContentManagerService.java"),serviceContent);

        String controllerPackageDot = StringUtils.replace(codeGeneratorConfig.getPackageConfig().getController(),".","/");
        File controllerPackage = new File(javaDir,controllerPackageDot);
        String controllerContent = VelocityUtil.formatFile("/com/sanri/config/templates/springboot/ContentManagerController.java", serviceExtendConfigs);
        FileUtils.writeStringToFile(new File(controllerPackage,"ContentManagerController.java"),controllerContent);

        return targetDir.getName()+"/"+codeGeneratorConfig.getProjectName();
    }

    /**
     * 解析 接口文件和实体文件
     * @param serviceExtendConfigs
     * @param generatedJavaFiles
     * @param codeGeneratorConfig
     * @return
     */
    private GeneratedInfo parserJavaFiles(List<GeneratedJavaFile> generatedJavaFiles, CodeGeneratorConfig codeGeneratorConfig) {
        String mapper = codeGeneratorConfig.getPackageConfig().getMapper();
        GeneratedInfo generatedInfo = new GeneratedInfo();
        for (GeneratedJavaFile generatedJavaFile : generatedJavaFiles) {
            String targetPackage = generatedJavaFile.getTargetPackage();
            String fileName = generatedJavaFile.getFileName();
            String baseName = FilenameUtils.getBaseName(fileName);

            if(targetPackage.equals(mapper)){
                // mapper 文件，映射实体类和映射文件
                generatedInfo.addMapper(baseName);
                //获取 tableClass
                Field tableClassField = FieldUtils.getDeclaredField(GenerateByTemplateFile.class, "tableClass", true);
                TableClass tableClass = (TableClass) ReflectionUtils.getField(tableClassField, generatedJavaFile);
                generatedInfo.addMapper(tableClass.getShortClassName(),baseName);
            }else{
                generatedInfo.addEntity(baseName);
            }
        }
        return generatedInfo;
    }

    private void buildFromSpringBoot(CodeGeneratorConfig codeGeneratorConfig, File targetDir) throws IOException {
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
        IOUtils.closeQuietly(fileOutputStream);
        IOUtils.closeQuietly(inputStream);
        //解压文件
        ZipUtil.unzip(springProjectFile,targetDir.getAbsolutePath());
        //删除压缩包
        FileUtils.forceDelete(springProjectFile);
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
