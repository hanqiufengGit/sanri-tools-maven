package com.sanri.app.version;

import freemarker.template.Version;

import java.io.File;
import java.util.List;

public interface VersionApi {
    /**
     * 最新版本
     * @return
     */
    Version latest();

    /**
     * 下载版本
     * @param version
     * @return
     */
    File download(Version version);
}
