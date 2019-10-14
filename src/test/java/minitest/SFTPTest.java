package minitest;

import com.jcraft.jsch.*;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Properties;

public class SFTPTest {

    @Test
    public void testUpload() throws JSchException, IOException, SftpException {
        JSch jsch = new JSch();
        Session session = jsch.getSession("ftpadmin","10.101.70.202",22);
        session.setPassword("salt202");

        Properties config = new Properties();
        // 不验证 HostKey
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect();

        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();

        FileInputStream fileInputStream = new FileInputStream("d:/test/1567993671544.xlsx");

        Path path = Paths.get("data","20191009");
        Iterator<Path> iterator = path.iterator();
        while (iterator.hasNext()){
            Path currentPath = iterator.next();
        }


//        channel.mkdir("data");
//        channel.cd("data");
//        channel.mkdir("abc");
//        channel.cd("abc");
        channel.cd("/data/package_dispatch/scp-st-informationreleaseapp");

        channel.put(fileInputStream,"mm.zip",ChannelSftp.APPEND);
        fileInputStream.close();
//        SftpATTRS sftpATTRS = channel.lstat("data/abc/mm");
//        long size = sftpATTRS.getSize();
//        System.out.println(size);
    }

    @Test
    public void test() throws URISyntaxException {
        URI uri = new URI("/app/a/b");
        System.out.println(new URI("/app").relativize(uri));
    }

}
