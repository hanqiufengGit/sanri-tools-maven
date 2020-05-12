package learntest;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

public class TestRef {
    public static void main(String[] args) {
        SoftReference<Object> soft = new SoftReference<>(new Object());
        WeakReference<Object> weak = new WeakReference<>(new Object());
        WeakReference<String> weakString = new WeakReference<>("abc");
        PhantomReference<Object> phantom = new PhantomReference<>(new Object(), new ReferenceQueue<>());

        System.out.println("GC 发生之前");
        System.out.println(soft.get());
        System.out.println(weak.get());
        System.out.println(weakString.get());
        System.out.println(phantom.get());
        System.gc();

        System.out.println("GC 发生之后 ");
        System.out.println(soft.get());
        System.out.println(weak.get());
        System.out.println(weakString.get());
        System.out.println(phantom.get());
    }
}
