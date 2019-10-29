package minitest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImageTypeCheck {
    static final Map<MimeType,String> imageMagicMap = new ConcurrentHashMap<>();
    static {
        imageMagicMap.put(MimeTypeUtils.parseMimeType("image/png"),"89504e47");
        imageMagicMap.put(MimeTypeUtils.parseMimeType("image/vnd.dwg"),"41433130313500000000");
        imageMagicMap.put(MimeTypeUtils.parseMimeType("image/tif"),"49492a00227105008037");
        imageMagicMap.put(MimeTypeUtils.parseMimeType("image/bmp"),"424d8e1b030000000000");
        imageMagicMap.put(MimeTypeUtils.parseMimeType("image/gif"),"4749463839");
        imageMagicMap.put(MimeTypeUtils.parseMimeType("image/bmp"),"424d228c010000000000");
        imageMagicMap.put(MimeTypeUtils.parseMimeType("image/bmp"),"424d8240090000000000");
        imageMagicMap.put(MimeTypeUtils.parseMimeType("image/jpg"),"ffd8ff");
        imageMagicMap.put(MimeTypeUtils.parseMimeType("image/gif"),"4749463837");
    }

    /**
     * 判断是否真的是图片文件
     * @param fileMimeType
     * @param multipartFile
     * @return
     */
    private boolean isImageMagic(MimeType fileMimeType) {
        try {
            InputStream inputStream = new FileInputStream("C:\\Users\\091795960\\Pictures/5.5KB.jpg");
            Iterator<Map.Entry<MimeType, String>> iterator = imageMagicMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<MimeType, String> next = iterator.next();
                MimeType key = next.getKey();
                String value = next.getValue();

                if(key.isCompatibleWith(fileMimeType)){
                    byte [] buffer28 = new byte[28];
                    try {
                        inputStream.read(buffer28);

                        char[] chars = Hex.encodeHex(buffer28);
                        return new String(chars).startsWith(value);
                    }catch (IOException e){ e.printStackTrace();}
                    finally {
                        IOUtils.closeQuietly(inputStream);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Test
    public void testImage(){
        boolean imageMagic = isImageMagic(MimeTypeUtils.parseMimeType("image/jpg"));
        System.out.println(imageMagic);
    }
}
