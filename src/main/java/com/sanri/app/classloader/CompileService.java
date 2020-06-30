package com.sanri.app.classloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

/**
 * 编译工具,对于给 Java 类的,提取包名类名和字段名信息
 * 然后重新生成 Java 类,进行编译,对于有复杂类型是无法处理的
 * 最后使用类加载器进行加载
 */
public class CompileService {
    private Logger log = LoggerFactory.getLogger(CompileService.class);
    /**
     * 编译 java 类,使用字符串传 java 类过来
     * @param javaCodes
     * @param className
     * @return
     */
    public Class<?> compile(String className,String javaCodes) throws ClassNotFoundException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null,null,null);
        StringObject stringObject = new StringObject(className, javaCodes);
        JavaCompiler.CompilationTask compilerTask = compiler.getTask(null, fileManager, null, null, null, Arrays.asList(stringObject));
        Boolean call = compilerTask.call();
        if(call){
            log.info("[{}] 编译成功",className);
            return Class.forName(className);
        }
        return null;
    }

    static class StringObject extends SimpleJavaFileObject {
        private String contents = null;

        public StringObject(String className, String contents) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.contents = contents;
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors)throws IOException {
            return contents;
        }
    }

}
