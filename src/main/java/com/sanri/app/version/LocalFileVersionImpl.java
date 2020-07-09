package com.sanri.app.version;

import com.sanri.app.BaseServlet;
import freemarker.template.Version;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LocalFileVersionImpl implements VersionApi {
    private File downloadPath;

    public LocalFileVersionImpl() {
        downloadPath = BaseServlet.mkTmpPath("version/update");
    }

    @Override
    public Version latest() {
        File updatesDir = downloadPath;
        String[] updates = updatesDir.list();
        if(updates.length == 0) {

            return null;
        }
        // 映射
        List<Version> versions = new ArrayList<>(updates.length);
        for (String version : updates) {
            versions.add(new Version(version));
        }

        // 排序
        Collections.sort(versions, new Comparator<Version>() {
            @Override
            public int compare(Version o1, Version o2) {
                return o1.intValue() - o2.intValue();
            }
        });

        return versions.get(0);
    }

    @Override
    public File download(Version version) {
        return new File(downloadPath,version.toString());
    }
}
