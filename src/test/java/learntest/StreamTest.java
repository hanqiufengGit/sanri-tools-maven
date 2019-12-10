package learntest;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class StreamTest {

    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(1,2,3,4,5);
        list.stream().map(x->2 * x).forEach(System.out::print);

       byte b1=1;
        byte b2=3;
        int b3=b1+b2;

        System.out.println(b3);
    }
}
