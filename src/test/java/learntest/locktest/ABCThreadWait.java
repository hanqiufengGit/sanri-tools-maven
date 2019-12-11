package learntest.locktest;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class ABCThreadWait {
    private static Object LockA = new Object();
    private static Object lockB = new Object();
    private static Object lockC = new Object();

    // 这里只是让三个线程同时开始
    static  CyclicBarrier cyclicBarrier = new CyclicBarrier(3);

    static class ThreadA extends Thread{
        @Override
        public void run() {
            try {
                cyclicBarrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 10; i++) {
                synchronized (lockC) {
                    synchronized (lockB) {
                        System.out.print("A");
                    }
                }
            }

        }
    }

    static class ThreadB extends Thread{
        @Override
        public void run() {
            try {
                cyclicBarrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 10; i++) {
                System.out.print("B");
            }
        }
    }
    static class ThreadC extends Thread{
        @Override
        public void run() {
            try {
                cyclicBarrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 10; i++) {
                System.out.print("C");
            }
        }
    }

    public static void main(String[] args) {
        new ThreadA().start();
        new ThreadB().start();
        new ThreadC().start();
    }
}
