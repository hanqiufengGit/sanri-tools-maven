package com.sanri.app.servlet;

import com.sanri.app.BaseServlet;
import com.sanri.app.version.CheckUpdate;
import com.sanri.frame.RequestMapping;
import freemarker.template.Version;

import java.io.IOException;

/**
 * 当前工具的版本管理
 */
@RequestMapping("/version")
public class VersionServlet extends BaseServlet {
    CheckUpdate checkUpdate = CheckUpdate.getInstance();

    public VersionServlet(){
        try {
            Version current = checkUpdate.current();
            Version latest = checkUpdate.latest();
            logger.info("当前工具版本 {} , 最版本为 {}",current,latest);
        } catch (IOException e) {}
    }

    /**
     * 当前版本
     * @return
     */
    public Version current() throws IOException {
        Version current = checkUpdate.current();
        return current;
    }

    /**
     * 最新版本
     * @return
     */
    public Version latest(){
        Version latest = checkUpdate.latest();
       return latest;
    }

    /**
     * 检查更新
     * @return
     */
    public int checkUpdate(){
        checkUpdate.checkUpdate();
        return 0 ;
    }
}
