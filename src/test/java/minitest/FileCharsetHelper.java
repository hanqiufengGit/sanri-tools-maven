package minitest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileCharsetHelper {

    /**
     * 读文件魔术数字来读取文件编码
     * 会关闭流
     * @param inputStream
     * @return
     */
    public static String fileCharset(InputStream inputStream) throws IOException {
        //默认GBK
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try(BufferedInputStream bis = new BufferedInputStream(inputStream)) {
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            // 文件编码为 ANSI
            if (read == -1) {
                return charset;
            }
            // 文件编码为 Unicode
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                return  "UTF-16LE";
            }
            // 文件编码为 Unicode big endian
            if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
                return "UTF-16BE";
            }
            // 文件编码为 UTF-8
            if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF) {
                return "UTF-8";
            }
            bis.reset();

            int loc = 0;
            while ((read = bis.read()) != -1) {
                loc++;
                if (read >= 0xF0) {
                    break;
                }
                // 单独出现BF以下的，也算是GBK
                if (0x80 <= read && read <= 0xBF) {
                    break;
                }
                if (0xC0 <= read && read <= 0xDF) {
                    read = bis.read();
                    // 双字节 (0xC0 - 0xDF)
                    if (0x80 <= read && read <= 0xBF) {
                        // (0x80
                        // - 0xBF),也可能在GB编码内
                        continue;
                    }
                    break;
                }
                // 也有可能出错，但是几率较小
                if (0xE0 <= read && read <= 0xEF) {
                    read = bis.read();
                    if (0x80 <= read && read <= 0xBF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            charset = "UTF-8";
                        }
                    }
                    break;
                }
            }
        } catch (IOException e) {
            throw e;
        }
        return charset;
    }
}
