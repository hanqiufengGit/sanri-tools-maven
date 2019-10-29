package minitest;

import info.monitorenter.cpdetector.io.*;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

public class EncodingTest {
    @Test
    public void testEncoding(){
        File file = new File("C:\\Users\\091795960\\Desktop/mimetypes.txt");
        ExchangeUtil bytesEncodingDetect = new ExchangeUtil();
        int encoding = bytesEncodingDetect.detectEncoding(file);
        String s = ExchangeUtil.javaname[encoding];
        System.out.println(s);
    }

    @Test
    public void testEncoding2() throws Exception {
        String encode = EncodeUtils.getEncode("C:\\Users\\091795960\\Desktop/mimetypes.txt", true);
        System.out.println(encode);
    }

    @Test
    public void test() throws IOException {
        String s = FileCharsetDetector.checkEncoding("C:\\Users\\091795960\\Desktop/mimetypes.txt");
        System.out.println(s);
    }

    /**
     * 方法三：比较准确，解决了实际问题
     * 只有在文件中有中文的情况下才能检测出来
     * @param filePath
     * @return
     */
    public static Charset getFileEncode(String filePath) {
        try {
            File file = new File(filePath);
            CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
            detector.add(new ParsingDetector(true));
            // 用到antlr.jar、chardet.jar
            detector.add(JChardetFacade.getInstance());
            // 测试 ascii和 unicode
            detector.add(ASCIIDetector.getInstance());
            detector.add(UnicodeDetector.getInstance());
            Charset charset = detector.detectCodepage(file.toURI().toURL());
            if (charset != null) {
                return charset;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    @Test
    public void testCharset(){
        System.out.println(getFileEncode("C:\\Users\\091795960\\Desktop/mimetypes.txt"));
    }

    @Test
    public void testSimple() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\091795960\\Desktop/mimetypes.txt");
        System.out.println(FileCharsetHelper.fileCharset(fileInputStream));
    }
}
