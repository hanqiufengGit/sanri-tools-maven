package learntest;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.sanri.app.classloader.CompileService;
import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.lang.model.element.Name;
import javax.lang.model.type.TypeKind;

public class JDKParser {

	static class FieldScanner extends TreeScanner<List<String>,List<String>>{
		@Override
		public List<String> visitVariable(VariableTree node, List<String> fields) {
			fields.add(node.toString());
			return fields;
		}
	}

	@Test
	public void testParse() throws IOException {
		Context context = new Context();
		JavacFileManager.preRegister(context);
		ParserFactory factory = ParserFactory.instance(context);

		String content = FileUtils.readFileToString(new File("d:/test/FileInfo.java"));
		JavacParser javacParser = factory.newParser(content, false, false, true);
		JCCompilationUnit jcCompilationUnit = javacParser.parseCompilationUnit();
		JCTree.JCExpression pid = jcCompilationUnit.pid;
		com.sun.tools.javac.util.Name name = ((JCTree.JCClassDecl) jcCompilationUnit.defs.get(0)).name;
		System.out.println(pid+"."+name);
		FieldScanner fieldScanner = new FieldScanner();
		List<String> fields = new ArrayList<>();
		fieldScanner.visitCompilationUnit(jcCompilationUnit, fields);
		System.out.println(fields);
	}

	@Test
	public void testCompile() throws IOException, ClassNotFoundException {
		CompileService compileService = new CompileService();
		String content = FileUtils.readFileToString(new File("d:/test/FileInfo.java"));
		Class<?> compile = compileService.compile("com.sanri.app.dtos.FileInfo", content);
		System.out.println(compile);
	}
}