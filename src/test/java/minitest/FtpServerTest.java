package minitest;

import javafx.scene.paint.Stop;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.net.ftp.FTPClient;

import java.io.FileInputStream;
import java.io.IOException;

public class FtpServerTest {
    public static void main(String[] args) throws IOException {
        FTPClient ftpClient = new FTPClient();
        StopWatch stopWatch  = new StopWatch();stopWatch.start();
        ftpClient.connect("localhost",21);
        ftpClient.login("appdeploy","Dev123#");
        boolean b = ftpClient.storeFile("b.png", new FileInputStream("C:\\Users\\091795960\\Desktop/V0147_2G.avi"));
        System.out.println(b+""+stopWatch.getTime()+" ms");
        stopWatch.stop();

    }
}
