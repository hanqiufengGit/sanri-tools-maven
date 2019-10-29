package minitest;

import org.junit.Test;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FormatTest {
    @Test
    public void test1(){
        MessageFormat sanri = new MessageFormat("{0}-{1}");
        System.out.println(sanri.getFormats().length);
    }
   static Map<String,String> fileTypeMap = new HashMap<>();

    @Test
    public void testPa(){
//        MimeType jpg = MimeTypeUtils.parseMimeType("image/*");
//        System.out.println(jpg.isCompatibleWith(MimeTypeUtils.parseMimeType("text/plain")));;
//        System.out.println(jpg);
        Iterator<Map.Entry<String, String>> iterator = fileTypeMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, String> next = iterator.next();
            String key = next.getKey();
            String value = next.getValue();
            System.out.println("imageMagicMap.put(MimeTypeUtils.parseMimeType(\""+value+"\"),\""+key+"\");");
        }
    }

    static {
        fileTypeMap.put("ffd8ff", "image/jpg"); // JPEG (jpg)
        fileTypeMap.put("89504e47", "image/png"); // PNG (png)
        fileTypeMap.put("4749463837", "image/gif"); // GIF (gif)
        fileTypeMap.put("4749463839", "image/gif"); // GIF (gif)
        fileTypeMap.put("49492a00227105008037", "image/tif"); // TIFF (tif)
        fileTypeMap.put("424d228c010000000000", "image/bmp"); // 16色位图(bmp)
        fileTypeMap.put("424d8240090000000000", "image/bmp"); // 24位位图(bmp)
        fileTypeMap.put("424d8e1b030000000000", "image/bmp"); // 256色位图(bmp)
        fileTypeMap.put("41433130313500000000", "image/vnd.dwg"); // CAD (dwg)
//        fileTypeMap.put("7b5c727466315c616e73", "text/rtf"); // Rich Text Format (rtf)
//        fileTypeMap.put("38425053000100000000", "application/octet-stream"); // Photoshop (psd)
//        fileTypeMap.put("46726f6d3a203d3f6762", "eml"); // Email [Outlook Express 6] (eml)
//        fileTypeMap.put("d0cf11e0a1b11ae10000", "doc"); // MS Excel 注意：word、msi 和 excel的文件头一样
//        fileTypeMap.put("5374616E64617264204A", "mdb"); // MS Access (mdb)
//        fileTypeMap.put("252150532D41646F6265", "ps");
//        fileTypeMap.put("255044462d312e", "pdf"); // Adobe Acrobat (pdf)
//        fileTypeMap.put("2e524d46000000120001", "rmvb"); // rmvb/rm相同
//        fileTypeMap.put("464c5601050000000900", "flv"); // flv与f4v相同
//        fileTypeMap.put("00000020667479706d70", "mp4");
//        fileTypeMap.put("49443303000000002176", "mp3");
//        fileTypeMap.put("000001ba210001000180", "mpg"); //
//        fileTypeMap.put("3026b2758e66cf11a6d9", "wmv"); // wmv与asf相同
//        fileTypeMap.put("52494646e27807005741", "wav"); // Wave (wav)
//        fileTypeMap.put("52494646d07d60074156", "avi");
//        fileTypeMap.put("4d546864000000060001", "mid"); // MIDI (mid)
//        fileTypeMap.put("526172211a0700cf9073", "rar");// WinRAR
//        fileTypeMap.put("235468697320636f6e66", "ini");
//        fileTypeMap.put("504B03040a0000000000", "jar");
//        fileTypeMap.put("504B0304140008000800", "jar");
//        fileTypeMap.put("D0CF11E0A1B11AE10", "xls");// xls文件
//        fileTypeMap.put("504B0304", "zip");
//        fileTypeMap.put("4d5a9000030000000400", "exe");// 可执行文件
//        fileTypeMap.put("3c25402070616765206c", "jsp");// jsp文件
//        fileTypeMap.put("4d616e69666573742d56", "mf");// MF文件
//        fileTypeMap.put("7061636b616765207765", "java");// java文件
//        fileTypeMap.put("406563686f206f66660d", "bat");// bat文件
//        fileTypeMap.put("1f8b0800000000000000", "gz");// gz文件
//        fileTypeMap.put("cafebabe0000002e0041", "class");// bat文件
//        fileTypeMap.put("49545346030000006000", "chm");// bat文件
//        fileTypeMap.put("04000000010000001300", "mxp");// bat文件
//        fileTypeMap.put("6431303a637265617465", "torrent");
//        fileTypeMap.put("6D6F6F76", "mov"); // Quicktime (mov)
//        fileTypeMap.put("FF575043", "wpd"); // WordPerfect (wpd)
//        fileTypeMap.put("CFAD12FEC5FD746F", "dbx"); // Outlook Express (dbx)
//        fileTypeMap.put("2142444E", "pst"); // Outlook (pst)
//        fileTypeMap.put("AC9EBD8F", "qdf"); // Quicken (qdf)
//        fileTypeMap.put("E3828596", "pwl"); // Windows Password (pwl)
//        fileTypeMap.put("2E7261FD", "ram"); // Real Audio (ram)
    }
}
