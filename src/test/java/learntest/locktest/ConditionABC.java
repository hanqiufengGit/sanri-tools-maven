package learntest.locktest;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionABC {
    static Lock lock = new ReentrantLock();
    static Condition conditionA  = lock.newCondition();
    static Condition conditionB  = lock.newCondition();
    static Condition conditionC  = lock.newCondition();

    static char lastChar = 'C';

   static CyclicBarrier cyclicBarrier = new CyclicBarrier(3);

    static class ThreadA extends Thread{
        @Override
        public void run() {
            try {
                cyclicBarrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 10; i++) {
                lock.lock();
                try {
                    if(lastChar != 'C'){
                        try {
                            conditionA.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.print("A");

                    lastChar = 'A';
                    conditionB.signalAll();
                }finally {
                    lock.unlock();
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
                lock.lock();
                try {

                    if(lastChar != 'A'){
                        try {
                            conditionB.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.print("B");

                    lastChar = 'B';
                    conditionC.signalAll();

                }finally {
                    lock.unlock();
                }
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
                lock.lock();
                try {
                    if(lastChar != 'B'){
                        try {
                            conditionC.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.print("C");

                    lastChar = 'C';
                    conditionA.signalAll();
                }finally {
                    lock.unlock();
                }
            }
        }
    }

    public static void main(String[] args) {
        new ThreadA().start();
        new ThreadB().start();
        new ThreadC().start();
    }
}
