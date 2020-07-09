package com.sanri.app.version;

import freemarker.template.Version;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StopWatch;
import sanri.utils.PathUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * 初始化的时候进行检查更新
 */
public class CheckUpdate {
    private Logger log = LoggerFactory.getLogger(CheckUpdate.class);
    private CheckUpdate(){}
    static CheckUpdate checkUpdate = new CheckUpdate();

    VersionApi versionApi = new LocalFileVersionImpl();

    public static CheckUpdate getInstance(){
        return checkUpdate;
    }

    /**
     * 检查更新
     * 1. 从 classpath:/version 获取当前项目版本
     * 2. 从配置的更新包路径获取所有的更新包,文件规则如下;
     *  packagePath
     *    |- 1.0.5
     *      |- updateEntry 文本文件,一行一条记录
     *         MODIFY WEB-INF/classes/com/sanri/app/servlet/XXServlet.class
     *         DELETE WEB-INF/lib/xx.jar
     *         ADD app/tools/xx.html
     *      |- changes 目录,里面的文件相对于根路径
     *    |- 2.0.3
     *    |- 0.0.7
     *  3. 只用获取最新版本, 每个版本会从在线更新版本开始,每次都是拿增量的完整包
     */
    public void checkUpdate(){
        Version latest = versionApi.latest();
        if(latest == null){
            log.error("未找到可更新的版本文件");
            throw new IllegalArgumentException("未找到可更新的版本文件");
        }
        try {
            Version current = current();
            if(current.intValue() >= latest.intValue()){
                log.error("当前版本[{}]新于最新版本[{}],将不会更新",current,latest);
                throw new IllegalArgumentException("更新包太旧");
            }
        } catch (IOException e) {
            return ;
        }
        log.info("将更新到 {} 版本",latest);

        File download = versionApi.download(latest);
        try {
            overwriteFiles(download);

            // 写入版本信息为最新版本
            File versionFile = ResourceUtils.getFile("classpath:/version");
            String version = FileUtils.readFileToString(versionFile);
            FileUtils.writeStringToFile(versionFile,latest.toString());

            log.info("版本更新成功 {} -> {}",version,latest);
        } catch (IOException e) {
            log.info("[{}]版本更新失败",latest,e);
            throw new IllegalArgumentException(latest+" 版本更新失败");
        }
    }

    /**
     * 覆盖目标文件
     * @param versionFiles
     */
    private void overwriteFiles(File versionFiles) throws IOException {
        // 读取文件更新清单,然后根据类型进行操作
        LineIterator lineIterator = FileUtils.lineIterator(new File(versionFiles, "updateEntry"));
        File changes = new File(versionFiles, "changes");

        while (lineIterator.hasNext()){
            String nextLine = lineIterator.nextLine();
            String[] split = StringUtils.split(nextLine, " ");
            if(split.length != 2){
                log.error("无效的操作1 [{}]",nextLine);
                continue;
            }
            String change = split[0].trim();
            FileModify fileModify = FileModify.parser(change);
            if(fileModify == null){
                log.error("无效的操作2 [{}]",nextLine);
                continue;
            }
            String relativePath = split[1].trim();
            changePathFile(fileModify,relativePath,changes);
        }

    }

    /**
     * 修改路径上的文件
     * @param fileModify
     * @param relativePath
     * @param changes
     */
    private void changePathFile(FileModify fileModify, String relativePath, File changes) throws IOException {
        File projectDir = new File(PathUtil.ROOT);
        File src = new File(changes, relativePath);
        File dest = new File(projectDir, relativePath);

        String fileName = dest.getName();
        String baseName = FilenameUtils.getBaseName(fileName);

        dest.getParentFile().mkdirs();
        switch (fileModify){
            case MODIFY:
                if(fileName.endsWith("class")){
                    // 删除相关文件,然后执行下面的 ADD 操作
                    File parentDir = dest.getParentFile();
                    // 列出类文件和内部类文件
                    WildcardFileFilter wildcardFileFilter = new WildcardFileFilter(baseName+"$*.class");
                    OrFileFilter orFileFilter = new OrFileFilter(wildcardFileFilter, new NameFileFilter(baseName+".class"));
                    Collection<File> files = FileUtils.listFiles(parentDir, orFileFilter, TrueFileFilter.TRUE);

                    for (File file : files) {
                        file.delete();
                    }
                }
            case ADD:
                if(fileName.endsWith("class")){
                    File parentDir = src.getParentFile();

                    // 列出类文件和内部类文件
                    WildcardFileFilter wildcardFileFilter = new WildcardFileFilter(baseName+"$*.class");
                    OrFileFilter orFileFilter = new OrFileFilter(wildcardFileFilter, new NameFileFilter(baseName+".class"));
                    Collection<File> files = FileUtils.listFiles(parentDir, orFileFilter, TrueFileFilter.TRUE);

                    // 复制目标文件到目标目录
                    for (File file : files) {
                        FileUtils.copyFile(file,new File(dest.getParentFile(),file.getName()));
                    }
                }else{
                    FileUtils.copyFile(src,dest);
                }
                break;
            case DELETE:
                if(fileName.endsWith("class")){
                    File parentDir = dest.getParentFile();

                    // 列出类文件和内部类文件
                    WildcardFileFilter wildcardFileFilter = new WildcardFileFilter(baseName+"$*.class");
                    OrFileFilter orFileFilter = new OrFileFilter(wildcardFileFilter, new NameFileFilter(baseName+".class"));
                    Collection<File> files = FileUtils.listFiles(parentDir, orFileFilter, TrueFileFilter.TRUE);
                    for (File file : files) {
                        file.delete();
                    }
                }else {
                    if(dest.exists()) {
                        FileUtils.forceDelete(dest);
                    }
                }
                break;
        }
    }

    enum FileModify{
        ADD("ADD"),DELETE("DELETE"),MODIFY("MODIFY");
        private String change;

        FileModify(String change) {
            this.change = change;
        }

        public static FileModify parser(String change){
            for (FileModify value : FileModify.values()) {
                if(value.change.equals(change)){
                    return value;
                }
            }
            return null;
        }
    }

    /**
     * 当前版本
     * @return
     * @throws IOException
     */
    public Version current() throws IOException {
        File versionFile = ResourceUtils.getFile("classpath:/version");
        String version = FileUtils.readFileToString(versionFile);
        return new Version(version);
    }

    /**
     * 获取最新版本
     * @return
     */
    public Version latest(){
        return versionApi.latest();
    }
}
