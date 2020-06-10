package com.sanri.initexec;

import com.sanri.app.ConfigCenter;
import freemarker.template.Version;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StopWatch;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 初始化的时候进行检查更新
 */
public class CheckUpdate {
    private Logger log = LoggerFactory.getLogger(CheckUpdate.class);

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
                    log.error("当前版本[{}]更新失败,不打算再继续往下更新,请确诊下载的包是否正确 ",version);
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
        // 读取文件更新清单
        String updateEntry = FileUtils.readFileToString(dir,"updateEntry");

        // 映射成更新列表

    }
}
