package minitest;

import org.apache.commons.lang3.time.DateUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class URITest {
    public static void main(String[] args) throws URISyntaxException, MalformedURLException, ParseException {
        URI uri = new URI("ftp://ftpadmin:salt202@10.101.70.202:21//scp-st-informationreleaseapp/20190917/1568705443741.txt");
        String host = uri.getHost();
        URI pathURI = new URI(uri.getPath());
//        URI relativize = pathURI.relativize(new URI(".."));
        URI relativize = new URI("../").relativize(pathURI);

        System.out.println(relativize);
        URL url = new URL("http", host, relativize.toString());
        System.out.println(url);

        String path = "/scp-st-informationreleaseapp/20190917/1568705443741.txt";
//        URI relativize = new URI("/scp-st-informationreleaseapp").relativize(new URI(path));
//        System.out.println(new URL("https","192.168.1.1","/"+relativize.getPath()));

    }
}
