package learntest.juc;

import java.util.concurrent.atomic.AtomicInteger;

public class TestCyclicBarrier {

	public static void main(String[] args) {
		Phase phase = new Phase();
		for(int i=0; i<3; i++) {
			new Thread(phase, "thread-" + i).start();
		}
	}
}

class Phase implements Runnable {
	
	private volatile AtomicInteger i1 = new AtomicInteger(3);
	
	private volatile AtomicInteger i2 = new AtomicInteger(3);
	
	private volatile AtomicInteger i3 = new AtomicInteger(3);

	@Override
	public void run() {
		m1();
		
	}

	private void m1() {
		System.out.println(Thread.currentThread().getName() + "======m1");
		i1.decrementAndGet();
		while(true) {
			if(i1.intValue() == 0) {
				m2();
				break;
			}
		}
	}
	
	private void m2() {
		System.out.println(Thread.currentThread().getName() + "======m2");
		i2.decrementAndGet();
		while(true) {
			if(i2.intValue() <= 0) {
				m3();
				break;
			}
		}
	}
	
	private void m3() {
		System.out.println(Thread.currentThread().getName() + "======m3");
		i3.decrementAndGet();
		while(true) {
			if(i3.intValue() <= 0) {
				m4();
				break;
			}
		}
	}
	
	private void m4() {
		System.out.println(Thread.currentThread().getName() + "======m4");
	}
}
