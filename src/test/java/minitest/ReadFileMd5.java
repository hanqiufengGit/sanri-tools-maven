package minitest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 只验证文件的前后 10m 的 md5 值
 */
public class ReadFileMd5 {
    private static int _2M = 2097152;
    @Test
    public void test() throws IOException, NoSuchAlgorithmException {
        StopWatch stopWatch = new StopWatch();stopWatch.start();

        File file = new File("C:\\Users\\091795960\\Downloads/15917562088468DnQpSz2vk.avi");
        RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r");
        FileChannel channel = randomAccessFile.getChannel();
        long size = channel.size();
        ByteBuffer buffer = ByteBuffer.allocate(_2M);		// 分配 2M 的大小
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");

        System.out.println("文件大小:"+size);

        // 读前面 10M  10 / 2 = 读 5 次
        int readCount = 5;
        while (readCount -- > 0){
            channel.read(buffer);
            buffer.flip();
            messageDigest.update(buffer);
            buffer.flip();
        }
        System.out.println("开始:0 结束 :"+randomAccessFile.getFilePointer());

        // 读后面 10M
        long startPosition = size - _2M * 5;
        channel.position(startPosition);
        readCount = 5 ;
        while (readCount --> 0 ){
            int read = channel.read(buffer);
            buffer.flip();
            messageDigest.update(buffer);
            buffer.flip();
        }
        System.out.println("开始:"+startPosition+" 结束 :"+randomAccessFile.getFilePointer());

        byte[] digest = messageDigest.digest();
        char[] chars = Hex.encodeHex(digest);
        String md5 = new String(chars);
        System.out.println(md5);

        stopWatch.stop();
        System.out.println("计算 md5 用时:"+stopWatch.getTime()+" ms");

        channel.close();
        randomAccessFile.close();
    }

    @Test
    public void testInte(){
        System.out.println(Integer.MAX_VALUE - 5 * 1024 * 1024 * 1024);
    }
}
