package com.sanri.app.classloader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import sanri.utils.ZipUtil;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClassLoaderManager {
    static Map<String,ExtendClassloader> CACHED_CLASSLOADER = new HashMap<>();

    /**
     * 加载压缩包里面的所有的类
     * @param zip
     * @return
     */
    public ExtendClassloader loadZipClassess(File zip) throws MalformedURLException {
        ZipUtil.unzip(zip,zip.getParent());
        FileUtils.deleteQuietly(zip);

        String baseName = FilenameUtils.getBaseName(zip.getName());
        return loadClasses(new File(zip.getParentFile(),baseName));
    }


    public ExtendClassloader loadClasses(File dir) throws MalformedURLException {
        String classloaderName = RandomStringUtils.randomAlphabetic(10);
        ExtendClassloader extendClassloader = new ExtendClassloader(classloaderName, dir.toURI().toURL());
        // 由于自定义的一般比较少的类,所以在初始化的时候就加载所有的类
        String parentPath = dir.getPath();
        Collection<File> files = FileUtils.listFiles(dir, new String[]{"class"}, true);
        for (File file : files) {
            String path = file.getPath();
            try {
                String packagePath = new URI(parentPath).relativize(new URI(path)).toString();
                String className = FilenameUtils.getBaseName(packagePath);
                extendClassloader.loadClass(className);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        CACHED_CLASSLOADER.put(classloaderName,extendClassloader);
        return extendClassloader;
    }

    public void removeClassloader(String name){
        CACHED_CLASSLOADER.remove(name);
    }
}
