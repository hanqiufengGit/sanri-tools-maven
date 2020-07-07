package com.sanri.initexec;

import com.sanri.app.ConfigCenter;
import freemarker.template.Version;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StopWatch;
import sanri.utils.PathUtil;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 初始化的时候进行检查更新
 */
public class CheckUpdate {
    private Logger log = LoggerFactory.getLogger(CheckUpdate.class);

    /**
     * 检查更新
     * 1. 从 classpath:/version 获取当前项目版本
     * 2. 从配置的更新包路径获取所有的更新包,文件规则如下;
     *  packagePath
     *    |- 1.0.5
     *      |- updateEntry 文本文件,一行一条记录
     *         U WEB-INF/classes/com/sanri/app/servlet/XXServlet.class
     *         D WEB-INF/lib/xx.jar
     *         A app/tools/xx.html
     *      |- changes 目录,里面的文件相对于根路径
     *    |- 2.0.3
     *    |- 0.0.7
     *  3. 查找在当前版本之后的版本,按顺序从小到大进行更新
     */
    @PostConstruct
    public void checkUpdate(){
        // 获取当前版本信息
        Version currentVersion = null;
        File versionFile = null;
        try {
            versionFile = ResourceUtils.getFile("classpath:/version");
            String version = FileUtils.readFileToString(versionFile);
            log.info("当前 sanritools 版本[{}]",version);
            currentVersion = new Version(version);
        } catch (IOException e) {}

        // 检查是否有配置更新
        String path = ConfigCenter.getInstance().getString("function.open", "data.update.path");
        if(StringUtils.isBlank(path)){
            log.info("没有配置更新,跳过更新");
            return ;
        }

        // 获取需要更新的文件
        File updatesDir = new File(path);
        if(!updatesDir.exists()){updatesDir.mkdirs();return ;}
        String[] updates = updatesDir.list();

        // 开始进行时间统计
        StopWatch stopWatch = new StopWatch();

        // 映射成版本信息
        stopWatch.start("映射");
        List<Version> versions = new ArrayList<>(updates.length);
        for (String version : updates) {
            versions.add(new Version(version));
        }
        stopWatch.stop();

        // 版本号进行排序
        stopWatch.start("排序");
        Collections.sort(versions, new Comparator<Version>() {
            @Override
            public int compare(Version o1, Version o2) {
                return o1.intValue() - o2.intValue();
            }
        });
        stopWatch.stop();

        // 遍历所有版本,比较当前版本,从最后一个版本开始进行更新直到最新版本
        stopWatch.start("版本更新");
        Version lastVersion = null;
        for (Version version : versions) {
            if(currentVersion.intValue() < version.intValue()){
                try {
                    overwriteFiles(new File(updatesDir, version.toString()));
                }catch (IOException e){
                    log.error("当前版本[{}]更新失败,不打算再继续往下更新,请确认下载的包是否正确 ",version);
                    break;
                }
                lastVersion = version;
            }
        }

        // 写入项目版本号
        if(lastVersion != null){
            try {
                FileUtils.writeStringToFile(versionFile,lastVersion.toString());
            } catch (IOException ex) {}
        }
        stopWatch.stop();
        log.info("版本检查更新总共耗时:{} ms",stopWatch.getTotalTimeMillis());
    }

    /**
     * 覆盖目标文件
     * @param dir
     */
    private void overwriteFiles(File dir) throws IOException {
        // 读取文件更新清单,然后根据类型进行操作
        LineIterator lineIterator = FileUtils.lineIterator(new File(dir, "updateEntry"));
        File changes = new File(dir, "changes");

        while (lineIterator.hasNext()){
            String nextLine = lineIterator.nextLine();
            String[] split = StringUtils.split(nextLine, " ");
            if(split.length != 2){
                log.error("无效的操作1 [{}]",nextLine);
                continue;
            }
            char change = split[0].trim().charAt(0);
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
        File projectDir = new File(PathUtil.ROOT.resolve("../../"));
        File dest = new File(projectDir, relativePath);
        switch (fileModify){
            case ADD:
            case MODIFY:
                dest.getParentFile().mkdirs();
                File src = new File(changes, relativePath);
                FileUtils.copyFile(src,dest);
                break;
            case DELETE:
                FileUtils.forceDelete(dest);
                break;
        }
    }

    enum FileModify{
        ADD('A'),DELETE('D'),MODIFY('M');
        private char change;

        FileModify(char change) {
            this.change = change;
        }

        public static FileModify parser(char change){
            for (FileModify value : FileModify.values()) {
                if(value.change == change){
                    return value;
                }
            }
            return null;
        }
    }
}
