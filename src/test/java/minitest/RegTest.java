package minitest;

import org.apache.oro.text.regex.MalformedPatternException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegTest {
    public static void main(String[] args) throws MalformedPatternException {
//        Pattern pattern = Pattern.compile("(\\!00:00:00)");
//        Matcher matcher = pattern.matcher("00:00:01");
//        System.out.println(matcher.find());
//        System.out.println("00".matches("(?!00)"));
//        System.out.println("01".matches("(?!00)"));

//        System.out.println("00:00:01".matches("[0-9]{2}:[0-9]{2}:(?!00)"));
//        System.out.println("0".matches("(?!0)"));

//        Pattern pattern = new Perl5Compiler().compile("(\\?\\!00:00:00)");
//        Perl5Matcher matcher = new Perl5Matcher();
//        PatternMatcherInput matcherInput = new PatternMatcherInput("00:00:01");
//        boolean contains = matcher.contains(matcherInput, pattern);
//        System.out.println(contains);

//        System.out.println("Windows3".matches("Windows(?!95|98|NT|2000)"));

//        Pattern pattern = Pattern.compile("re(?!g)");
//        Matcher matcher = pattern.matcher("”regex represents regular expression”");
//        if (matcher.find()){
//            int i = matcher.groupCount();
//            for (int j = 0; j < i; j++) {
//                System.out.println(matcher.group(j));
//            }
//        }

        Pattern validateExPressPattern=Pattern.compile("\\[url\\]((?=.*\\[url\\]).*)\\[/url\\]", Pattern.CASE_INSENSITIVE);
        Matcher m = validateExPressPattern.matcher("[url][b]123[/b][/url]");
        System.out.println("----------"+m.matches());
        System.out.println(m.group(1));
    }
}
