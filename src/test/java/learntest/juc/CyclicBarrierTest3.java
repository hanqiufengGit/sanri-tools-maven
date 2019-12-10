package learntest.juc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 来自贴子  https://bbs.csdn.net/topics/390820778
 * 循环栅栏控制例子
 * 体现为什么是循环使用的 barrier.reset(); 可以重置栅栏
 */
public class CyclicBarrierTest3 {

	public CyclicBarrierTest3() throws BrokenBarrierException, InterruptedException {
		CyclicBarrier barrier = new CyclicBarrier(3, new Runnable() {
			@Override
			public void run() {
				System.out.println("所有线程都执行完了！");
			}
		});

		ExecutorService exec = Executors.newFixedThreadPool(2);

		for (int i = 0; i < 10; i++) {
			barrier.reset();

			Task t1 = new Task(barrier, 1, 3000);
			Task t2 = new Task(barrier, 2, 1000);

			exec.submit(t1);
			exec.submit(t2);

			// 控制线程也需要参与等待
			barrier.await();
		}


		exec.shutdown();
	}


	static String now() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		return sdf.format(new Date()) + ": ";
	}

	static class Task implements Runnable {

		private int threadNo = -1;
		private int time = 0;

		private CyclicBarrier barrier;

		public Task(CyclicBarrier barrier, int threadNo, int time) {
			this.barrier = barrier;
			this.threadNo = threadNo;
			this.time = time;
		}

		public void run() {
			try {
				System.out.println(now() + "第" + threadNo + "号线程在执行");
				Thread.sleep(time);
				System.out.println(now() + "第" + threadNo + "号线程执行完毕!!!  花费" + (time / 1000) + "秒");
				barrier.await();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public static void main(String[] args) throws BrokenBarrierException, InterruptedException {
			new CyclicBarrierTest3();
		}
	}
}