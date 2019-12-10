package learntest.locktest;

import org.apache.commons.lang3.RandomStringUtils;
import sun.java2d.SurfaceDataProxy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * 测试生产者消费者
 */
public class QueueUseWaitNotify {
    private static Buffer buffer = new Buffer();

    static class Producer extends Thread{
        private CountDownLatch countDownLatch;
        public Producer(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            synchronized (buffer) {
                while (buffer.isFull()) {
                    try {
                        buffer.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                buffer.add();
                buffer.notifyAll();
//                buffer.notify();
            }
            countDownLatch.countDown();
        }
    }

    static class Consumer extends Thread{
        private CountDownLatch countDownLatch;
        public Consumer(CountDownLatch countDownLatch){
            this.countDownLatch = countDownLatch;
        }
        @Override
        public void run() {
            synchronized (buffer){
                while (buffer.isEmpty()){
                    try {
                        buffer.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                buffer.remove();
                buffer.notifyAll();
//                buffer.notify();
            }
            countDownLatch.countDown();
        }
    }

    private static class Buffer {
        private static final int MAX_CAPACITY = 1;
        private List innerList = new ArrayList<>(MAX_CAPACITY);

        void add() {
            if (isFull()) {
                throw new IndexOutOfBoundsException();
            } else {
                innerList.add(RandomStringUtils.randomNumeric(5));
            }
            System.out.println(Thread.currentThread().toString() + " add " +innerList);

        }

        void remove() {
            if (isEmpty()) {
                throw new IndexOutOfBoundsException();
            } else {
                innerList.remove(MAX_CAPACITY - 1);
            }
            System.out.println(Thread.currentThread().toString() + " remove " +innerList);
        }

        boolean isEmpty() {
            return innerList.isEmpty();
        }

        boolean isFull() {
            return innerList.size() == MAX_CAPACITY;
        }
    }


    public static void main(String[] args) throws InterruptedException {
        for (int j = 0; j < 1000000; j++) {
            CountDownLatch countDownLatch = new CountDownLatch(4);
            for (int i = 0; i < 2; i++) {
                new Producer(countDownLatch).start();
            }

            for (int i = 0; i < 2; i++) {
                new Consumer(countDownLatch).start();
            }
            countDownLatch.await();
            System.out.println("---------"+j+"-----------"+buffer.innerList);
        }


    }

}
