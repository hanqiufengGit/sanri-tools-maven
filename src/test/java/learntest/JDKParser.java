//package learntest;
//
//import com.sun.source.tree.ModifiersTree;
//import com.sun.source.tree.VariableTree;
//import com.sun.source.util.TreeScanner;
//
//import java.util.List;
//
//
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.ByteBuffer;
//import java.nio.channels.FileChannel;
//import java.nio.channels.FileChannel.MapMode;
//import java.nio.charset.Charset;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import com.sanri.app.classloader.CompileService;
//import com.sun.source.tree.*;
//import com.sun.source.util.TreeScanner;
//import com.sun.tools.javac.file.JavacFileManager;
//import com.sun.tools.javac.parser.JavacParser;
//import com.sun.tools.javac.parser.Parser;
//import com.sun.tools.javac.parser.ParserFactory;
//import com.sun.tools.javac.tree.JCTree;
//import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
//import com.sun.tools.javac.util.Context;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.IOUtils;
//import org.junit.Assert;
//import org.junit.Test;
//
//import javax.lang.model.element.Name;
//import javax.lang.model.type.TypeKind;
//import javax.tools.*;
//
//public class JDKParser {
//
//	static class FieldScanner extends TreeScanner<List<String>,List<String>> {
//		@Override
//		public List<String> visitVariable(VariableTree node, List<String> fields) {
//            ModifiersTree modifiers = node.getModifiers();
//            fields.add(node.toString());
//			return fields;
//		}
//	}
//
//	@Test
//	public void testParse() throws IOException {
//		Context context = new Context();
//		JavacFileManager.preRegister(context);
//		ParserFactory factory = ParserFactory.instance(context);
//
//		String content = FileUtils.readFileToString(new File("d:/test/FileInfo.java"));
//		JavacParser javacParser = factory.newParser(content, false, false, true);
//		JCCompilationUnit jcCompilationUnit = javacParser.parseCompilationUnit();
//		JCTree.JCExpression pid = jcCompilationUnit.pid;
//		com.sun.tools.javac.util.Name name = ((JCTree.JCClassDecl) jcCompilationUnit.defs.get(0)).name;
//		System.out.println(pid+"."+name);
//		FieldScanner fieldScanner = new FieldScanner();
//		List<String> fields = new ArrayList<>();
//		fieldScanner.visitCompilationUnit(jcCompilationUnit, fields);
//		System.out.println(fields);
//	}
//
//	@Test
//	public void testCompile() throws IOException, ClassNotFoundException {
//		CompileService compileService = new CompileService();
//		String content = FileUtils.readFileToString(new File("d:/test/FileInfo.java"));
//		compileService.compile("com.sanri.app.dtos.FileInfo", content);
//	}
//
//	@Test
//	public void testCompileApi() throws IOException {
//		JavaCompiler systemJavaCompiler = ToolProvider.getSystemJavaCompiler();
//		// 建立 DiagnosticCollector 对象
//		DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<JavaFileObject>();
//		StandardJavaFileManager standardFileManager = systemJavaCompiler.getStandardFileManager(diagnosticCollector, null, null);
//		// 建立用于保存被编译文件名的对象,每个文件被保存在一个从 JavaFileObject 继承的类中
//		Iterable<? extends JavaFileObject> compilationUnits = standardFileManager.getJavaFileObjectsFromStrings(Arrays.asList("test3.java"));
//		JavaCompiler.CompilationTask task = systemJavaCompiler.getTask(null, standardFileManager, diagnosticCollector, null, null, compilationUnits);
//		Boolean success = task.call();
//		standardFileManager.close();
//		Assert.assertTrue(success);
//	}
//}