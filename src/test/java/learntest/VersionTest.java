package learntest;

import freemarker.template.Version;
import org.junit.Test;

public class VersionTest {
    @Test
    public void testVersion(){
        Version version = new Version("1.4.0");
        System.out.println(version.getMajor());
        System.out.println(version.getMinor());
        System.out.println(version.getMicro());

        System.out.println(version.intValue());
    }
}
