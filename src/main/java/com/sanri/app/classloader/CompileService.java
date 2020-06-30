package com.sanri.app.classloader;

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
            System.out.println("编译成功");
            return Class.forName(className);
        }
        return null;
    }

    class StringObject extends SimpleJavaFileObject {
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
