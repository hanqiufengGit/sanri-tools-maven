package minitest;

import org.junit.Test;
import sanri.utils.StringUtilsExtend;

public class StringUtilExtendsTest {
    @Test
    public void test(){
        System.out.println(StringUtilsExtend.readableFileSize(2314231));
    }
}
