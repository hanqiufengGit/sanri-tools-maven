package com.sanri.app.servlet;

import com.sanri.app.BaseServlet;
import com.sanri.app.classloader.ClassLoaderManager;
import com.sanri.app.classloader.CompileService;
import com.sanri.app.jdbc.codegenerate.SimpleJavaBeanBuilder;
import com.sanri.frame.DispatchServlet;
import com.sanri.frame.RequestMapping;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import sanri.utils.ZipUtil;

import java.io.*;
import java.util.Collection;
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
    private CompileService compileService = new CompileService();

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
        }else if("zip".equalsIgnoreCase(extension) || "zip2".equalsIgnoreCase(extension)){
            // classloader/xx.zip
            targetFile =  new File(modulDir, fileItem.getName());
        }

        // 复制 class 或 zip 到目标路径
        if(targetFile != null) {
            FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
            try {
                IOUtils.copy(fileItem.getInputStream(), fileOutputStream);
            } finally {
                IOUtils.closeQuietly(fileOutputStream);
            }
        }

        // zip2 表示是已经处理好目录的 class 文件,zip 文件是没有处理好目录的 class 文件列表
        if("zip2".equalsIgnoreCase(extension)) {
            classLoaderManager.loadZipClassess(targetFile, title);
        }else if("class".equalsIgnoreCase(extension)){
            classLoaderManager.loadSingleClass(targetFile);
        }else if("java".equalsIgnoreCase(extension)){
            InputStream inputStream = fileItem.getInputStream();
            String content = IOUtils.toString(inputStream);
            SimpleJavaBeanBuilder simpleJavaBeanBuilder = compileService.javaBeanAdapter(content);
            logger.info("编译并加载 bean : [{}]",simpleJavaBeanBuilder.getClassName());
            compileService.compile(simpleJavaBeanBuilder);
            classLoaderManager.loadClasses(singleClassesDir,"singleClasses");
        }else if("zip".equalsIgnoreCase(extension)){
            // 解压文件到当前目录
            File file = new File(modulDir, title);
            ZipUtil.unzip(targetFile,file.getAbsolutePath());
            targetFile.delete();        // 删除 zip 文件
            readPackageNameAndMove(file);

            // 加载到类加载器
            classLoaderManager.loadClasses(file,title);
        }
        return 0;
    }

    /**
     * 读取并创建所有类的包路径 ,然后移动文件
     * @param dir
     * @throws IOException
     */
    public void readPackageNameAndMove(File dir) throws IOException {
        Collection<File> files = FileUtils.listFiles(dir, new String[]{"class"}, false);
        for (File file : files) {
            FileInputStream fileInputStream = new FileInputStream(file);
            ClassReader reader = new ClassReader(fileInputStream);
            ClassNode classNode = new ClassNode();//创建ClassNode,读取的信息会封装到这个类里面
            reader.accept(classNode, 0);//开始读取
            fileInputStream.close();

            // 创建包路径
            File classFileNoSuffix = new File(dir, classNode.name+".class");
            classFileNoSuffix.getParentFile().mkdirs();

            // 移动文件
            FileUtils.copyFile(file,classFileNoSuffix);
            // 删除原来文件
            FileUtils.deleteQuietly(file);
        }
    }

    static {
        modulDir = mkTmpPath(modul);
        singleClassesDir = mkTmpPath(modul+"/singleClasses");
    }
}
