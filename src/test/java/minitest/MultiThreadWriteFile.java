package minitest;

import jdk.internal.util.xml.impl.Input;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sanri.utils.NumberUtil;

import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 多线程分段写文件
 */
public class MultiThreadWriteFile {
    private URL url;
    // 默认 10k 一段
    private int segmentSize = 10240;

    private Logger log = LoggerFactory.getLogger(getClass());

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    public MultiThreadWriteFile(URL url) {
        this.url = url;
    }

    public MultiThreadWriteFile(URL url, int segmentSize) {
        this.url = url;
        this.segmentSize = segmentSize;
    }

    /**
     * 写到目标文件
     * @param target
     */
    public void write(File target) throws IOException, InterruptedException {
        InputStream stream = url.openStream();
        int streamSize = stream.available();
        stream.close();

        // 线程数
        int pageSize = (streamSize - 1) / segmentSize + 1;
        System.out.println("总大小 :"+streamSize+",分页数:"+pageSize);
        CountDownLatch countDownLatch = new CountDownLatch(pageSize);

        for (int i = 0; i < pageSize; i++) {
            int finalI = i;
            executorService.submit(()->{
                RandomAccessFile randomAccessFile = null;
                try {
                    InputStream inputStream = url.openStream();
                    byte [] buff = new byte[segmentSize];
                    inputStream.skip(((long) finalI) * segmentSize);
                    int read =  -1;
                    if (finalI != pageSize - 1) {
                         read = inputStream.read(buff);
                    } else {
                        int re = inputStream.available() % segmentSize;
                        read = inputStream.read(buff, 0, re);
                    }

                    inputStream.close();
                    log.info(Thread.currentThread().getName()+" page:"+finalI+" seek:"+((long) finalI) * segmentSize+",read:"+read);

                    randomAccessFile = new RandomAccessFile(target,"rw");
                    randomAccessFile.seek(((long) finalI) * segmentSize);
                    randomAccessFile.write(buff);

                    log.info(Thread.currentThread().getName()+" 写入目标位置:"+randomAccessFile.getChannel().position());
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    countDownLatch.countDown();
                    log.info(Thread.currentThread().getName()+"已经写入 "+(finalI + 1)+"/"+pageSize+",写入进度:"+ NumberUtil.percent(finalI + 1,pageSize,2) + " %");
                    if(randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        countDownLatch.await();
        log.info("文件写入成功");
        executorService.shutdown();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
//        URL url = new URL("file:///C:\\Users\\091795960\\Videos/V0082_3.3G终结者2.双语字幕.HR-HDTV.DTS.avi");
        URL url = new URL("file:///d:/test/a.avi");
        File file = new File("d:/test/a.txt");

        MultiThreadWriteFile multiThreadWriteFile = new MultiThreadWriteFile(url, 1024 * 1024);
        StopWatch stopWatch = new StopWatch();stopWatch.start();
        multiThreadWriteFile.write(file);
        System.out.println("写出文件用时："+stopWatch.getTime() + " ms");
//        RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
//        randomAccessFile.seek(1024);
//        randomAccessFile.write(2);
//        randomAccessFile.close();

//        System.out.println(fileInputStream.available());
//        byte [] buffer = new byte [1024];
//        fileInputStream.skip(1024);
//        int read = fileInputStream.read(buffer);
//        System.out.println(read);
//        System.out.println(new String(buffer));
//        fileInputStream.close();


    }
}
