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
    // redis service 中已经创建过了，先这样吧
    private ClassLoaderManager classLoaderManager = new ClassLoaderManager();
    private static String modul = "classloader";
    static File modulDir  = null;
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
        if(StringUtils.isBlank(title)){
            title = baseName;
        }
        File targetZipFile = new File(modulDir, fileItem.getName());
        FileOutputStream fileOutputStream = new FileOutputStream(targetZipFile);
        try {
            IOUtils.copy(fileItem.getInputStream(), fileOutputStream);
        }finally {
            fileOutputStream.flush();fileOutputStream.close();
        }

        classLoaderManager.loadZipClassess(targetZipFile,title);
        return 0;
    }

    static {
        modulDir = mkTmpPath(modul);
    }
}
