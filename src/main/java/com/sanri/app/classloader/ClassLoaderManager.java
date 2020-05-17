package com.sanri.app.classloader;

import com.sanri.app.BaseServlet;
import com.sanri.app.servlet.ClassLoaderServlet;
import com.sanri.app.servlet.FileManagerServlet;
import com.sanri.frame.DispatchServlet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.util.ReflectionUtils;
import sanri.utils.ZipUtil;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ClassLoaderManager {
    static Map<String,ExtendClassloader> CACHED_CLASSLOADER = new HashMap<>();
    private ClassLoaderManager(){}

    private static ClassLoaderManager classLoaderManager = null;
    public synchronized static ClassLoaderManager getInstance(){
        if(classLoaderManager == null) {
            classLoaderManager = new ClassLoaderManager();
            classLoaderManager.initLoadClasses();
        }
        return classLoaderManager;
    }
    /**
     * 加载压缩包里面的所有的类
     * @param zip
     * @return
     */
    public ExtendClassloader loadZipClassess(File zip,String title) throws MalformedURLException {
        File loadDir = new File(zip.getParentFile(), title);loadDir.mkdirs();
        ZipUtil.unzip(zip,loadDir.getAbsolutePath());
        FileUtils.deleteQuietly(zip);

        return loadClasses(loadDir,title);
    }

    public ExtendClassloader loadClasses(File dir, String title) throws MalformedURLException {
//        String classloaderName = RandomStringUtils.randomAlphabetic(10);
        ExtendClassloader extendClassloader = new ExtendClassloader(title, dir.toURI().toURL());
        // 由于自定义的一般比较少的类,所以在初始化的时候就加载所有的类
        URI parent = dir.toURI();
        Collection<File> files = FileUtils.listFiles(dir, new String[]{"class"}, true);
        for (File file : files) {
            URI path = file.toURI();
            try {
                String packagePath = parent.relativize(path).toString();
                String classPath = packagePath.replaceAll("/", ".");
                String className = FilenameUtils.getBaseName(classPath);
                extendClassloader.loadClass(className);
            }  catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        CACHED_CLASSLOADER.put(title,extendClassloader);
        return extendClassloader;
    }

    public void removeClassloader(String name){
        CACHED_CLASSLOADER.remove(name);
    }

    public Set<String> classloaders(){
        return CACHED_CLASSLOADER.keySet();
    }

    /**
     * 查看加载的类
     * @param name
     * @return
     */
    public Set<String> loadedClasses(String name){
        ExtendClassloader extendClassloader = CACHED_CLASSLOADER.get(name);
        Field classes = FieldUtils.getField(ClassLoader.class, "classes", true);
        Vector<Class<?>>  classVector = (Vector<Class<?>>)  ReflectionUtils.getField(classes, extendClassloader);
        Set<String> collect = classVector.stream().map(Class::getName).collect(Collectors.toSet());
        return collect;
    }

    public ExtendClassloader get(String classloaderName) {
        return CACHED_CLASSLOADER.get(classloaderName);
    }

    /**
     * 初始化时加载已经加载过的类
     */
    @PostConstruct
    public void initLoadClasses()  {
        File classloaderDir = BaseServlet.mkTmpPath(ClassLoaderServlet.modul);
        File[] files = classloaderDir.listFiles();
        for (File file : files) {
            String name = file.getName();
            try {
                loadClasses(file,name);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
