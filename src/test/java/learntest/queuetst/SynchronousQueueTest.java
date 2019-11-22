package learntest.queuetst;

import java.util.concurrent.SynchronousQueue;

public class SynchronousQueueTest {
    static SynchronousQueue synchronousQueue = new SynchronousQueue();

    static class A extends Thread{
        @Override
        public void run() {
            try {
                synchronousQueue.put("sanri");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class B extends Thread{
        @Override
        public void run() {
            Object peek = synchronousQueue.peek();
            System.out.println(peek);
        }
    }

    public static void main(String[] args) {
        new B().start();
    }
}
