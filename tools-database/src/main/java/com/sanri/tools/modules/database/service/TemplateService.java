package com.sanri.tools.modules.database.service;

import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.database.dtos.meta.ActualTableName;
import com.sanri.tools.modules.database.dtos.meta.Column;
import com.sanri.tools.modules.database.dtos.meta.TableMetaData;
import com.sanri.tools.modules.database.service.rename.JavaBeanInfo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 代码生成的模板管理
 * 分为单个模板管理和方案管理
 * 方案表示为组合多个模板
 */
@Service
public class TemplateService {
    @Autowired
    private FileManager fileManager;
    @Autowired
    private Configuration configuration;
    private FreeMarkerTemplate freeMarkerTemplate = new FreeMarkerTemplate();

    // 数据都存储在这个路径下,使用后缀来区分是方案还是模板 *.schema 是方案 , *.ftl 是 freemarker 模板 ,*.vm 是 velocity 模板
    private static final String basePath = "code/templates";
    private static final String[] SCHEMA_EXTENSION = {"schema"};
    private static final String[] TEMPLATE_EXTENSION = {"ftl","vm"};

    /** 方案,模板的增删改查 **/
    public List<String> schemas(){
        File dir = fileManager.mkTmpDir(basePath);
        Collection<File> files = FileUtils.listFiles(dir, SCHEMA_EXTENSION, false);
        List<String> collect = files.stream().map(File::getName).collect(Collectors.toList());
        return collect;
    }
    public List<String> templates(){
        File dir = fileManager.mkTmpDir(basePath);
        Collection<File> files = FileUtils.listFiles(dir, TEMPLATE_EXTENSION, false);
        List<String> collect = files.stream().map(File::getName).collect(Collectors.toList());
        return collect;
    }
    // 模板或者方案内容
    public String content(String name) throws IOException {
        return  FileUtils.readFileToString(new File(basePath, name));
    }
    // 方案依赖的模板列表
    public List<String> schemaTemplates(String name) throws IOException {
        return FileUtils.readLines(new File(basePath,name), StandardCharsets.UTF_8);
    }
    // 写入模板或方案
    public void writeContent(String name,String content) throws IOException {
        File file = new File(basePath, name);
        FileUtils.writeStringToFile(file,content);
    }
    // 上传一个模板
    public void uploadTemplate(MultipartFile file) throws IOException {
        File templateFile = new File(basePath, file.getName());
        file.transferTo(templateFile);
    }

    /**
     * 根据元数据和模板解析模板文件
     * @param template
     * @param currentTable
     * @param renameStrategy
     * @return
     */
    public String preview(String templateName, TableMetaData currentTable, RenameStrategy renameStrategy) throws IOException, TemplateException {
        ActualTableName actualTableName = currentTable.getActualTableName();
        Map<String, Object> context = new HashMap<>();
        context.put("meta",currentTable);
        JavaBeanInfo mapping = renameStrategy.mapping(currentTable);
        context.put("mapping",mapping);
        context.put("date", DateFormatUtils.ISO_DATE_FORMAT.format(System.currentTimeMillis()));
        context.put("time",DateFormatUtils.ISO_TIME_NO_T_FORMAT.format(System.currentTimeMillis()));
        context.put("author",System.getProperty("user.name"));
        Template template = configuration.getTemplate(templateName+".ftl");
        return freeMarkerTemplate.process(template,context);
    }

    /**
     * 根据需要的表, 生成模板代码
     * @param templateName
     * @param renameStrategy
     * @param filterTables
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public String processBatch(String templateName, RenameStrategy renameStrategy, List<TableMetaData> filterTables) throws IOException, TemplateException {
        Template template = configuration.getTemplate(templateName+".ftl");
        for (TableMetaData filterTable : filterTables) {
            ActualTableName actualTableName = filterTable.getActualTableName();
            Map<String, Object> context = new HashMap<>();
            context.put("meta",filterTable);
            JavaBeanInfo mapping = renameStrategy.mapping(filterTable);
            context.put("mapping",mapping);
            context.put("date", DateFormatUtils.ISO_DATE_FORMAT.format(System.currentTimeMillis()));
            context.put("time",DateFormatUtils.ISO_TIME_NO_T_FORMAT.format(System.currentTimeMillis()));
            context.put("author",System.getProperty("user.name"));
            String process = freeMarkerTemplate.process(template, context);

            // 写入目标文件
        }
        return null;
    }

    private class FreeMarkerTemplate {
        private static final String extension = "ftl";

        public String process(Template template, Map<String,Object> context) throws IOException, TemplateException {
            StringBuilderWriter stringBuilderWriter = new StringBuilderWriter();
            template.process(context,stringBuilderWriter);
            String text = stringBuilderWriter.toString();
            return text;
        }
    }
}
