package com.sanri.app.servlet;

import com.sanri.app.BaseServlet;
import com.sanri.app.classloader.ClassLoaderManager;
import com.sanri.frame.DispatchServlet;
import com.sanri.frame.RequestMapping;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * 类加载器服务
 */
@RequestMapping("/classloader")
public class ClassLoaderServlet extends BaseServlet {
    private ClassLoaderManager classLoaderManager = ClassLoaderManager.getInstance();
    public static String modul = "classloader";
    public static File modulDir  = null;
    public static File singleClassesDir = null;
    private FileManagerServlet fileManagerServlet = DispatchServlet.getServlet(FileManagerServlet.class);

    public Set<String> classloaders(){
        return classLoaderManager.classloaders();
    }

    public Set<String> loadedClasses(String name){
        return classLoaderManager.loadedClasses(name);
    }

    /**
     * 上传类字节码
     * @param title
     * @param fileItem
     * @return
     */
    public int uploadClasses(String title, FileItem fileItem) throws IOException {
        String baseName = FilenameUtils.getBaseName(fileItem.getName());
        String extension = FilenameUtils.getExtension(fileItem.getName());

        if(StringUtils.isBlank(title)){
            title = baseName;
        }

        File targetFile = null;
        if("class".equalsIgnoreCase(extension)){
            // classloader/singleClasses/xx.class
            targetFile = new File(singleClassesDir,fileItem.getName());
        }else if("zip".equalsIgnoreCase(extension)){
            // classloader/xx.zip
            targetFile =  new File(modulDir, fileItem.getName());
        }

        // 复制 class 或 zip 到目标路径
        FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
        try {
            IOUtils.copy(fileItem.getInputStream(), fileOutputStream);
        }finally {
           IOUtils.closeQuietly(fileOutputStream);
        }

        if("zip".equalsIgnoreCase(extension)) {
            classLoaderManager.loadZipClassess(targetFile, title);
        }else if("class".equalsIgnoreCase(extension)){
            classLoaderManager.loadSingleClass(targetFile);
        }else if("java".equalsIgnoreCase(extension)){

        }
        return 0;
    }

    static {
        modulDir = mkTmpPath(modul);
        singleClassesDir = mkTmpPath(modul+"/singleClasses");
    }
}
