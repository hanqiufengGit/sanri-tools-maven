package minitest;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;

public class CalendarTest {
    @Test
    public void testAddDate(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -8);
        System.out.println(DateFormatUtils.format(cal, "yyyy-MM-dd"));
    }

    @Test
    public void testP() throws URISyntaxException {
        URI uri = new URI("https://www.xsbiquge.com/81_81211/");
        URI uri1 = new URI("/81_81211/221000.html");
        System.out.println(uri.resolve(uri1));
    }
}
