package learntest.juc;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class TravelTaskPerson implements Runnable {
    private CyclicBarrier cyclicBarrier;
    private String name;
    private Date arriveTime;//赶到的时间

    public TravelTaskPerson(CyclicBarrier cyclicBarrier, String name, Date arriveTime) {
        this.cyclicBarrier = cyclicBarrier;
        this.name = name;
        this.arriveTime = arriveTime;
    }

    @Override
    public void run() {
        String format = DateFormatUtils.format(arriveTime, "yyyy-MM-dd HH:mm:ss");
        System.out.println(name+" 在 "+ format + " 时间赶到了集合点，等下其它人| 这时候个人的线程是阻塞的，countDownLanch 不是阻塞的,countdownlanch 线程已经结束了");
        try {
            cyclicBarrier.await();
            System.out.println(name +"开始和大伙一起旅行啦～～");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }

    }
}
