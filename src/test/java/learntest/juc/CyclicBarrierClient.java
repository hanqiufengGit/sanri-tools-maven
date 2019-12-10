package learntest.juc;

import sanri.utils.RandomUtil;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CyclicBarrierClient {

        public static void main(String[] args) throws Exception{

            // 如果 parties 数量少于实际线程数量 ，则最后一个到的线程会无限等待; 相当于来晚了，车已经走了，虽然有三个人，但属于旅游团性质，到点不等人
            CyclicBarrier cyclicBarrier = new CyclicBarrier(3,new TourGuideTask("哈登","保罗","戈登"));

            ExecutorService executor = Executors.newFixedThreadPool(3);
            executor.execute(new TravelTaskPerson(cyclicBarrier,"哈登",RandomUtil.date()));
            executor.execute(new TravelTaskPerson(cyclicBarrier,"保罗",RandomUtil.date()));
            executor.execute(new TravelTaskPerson(cyclicBarrier,"戈登",RandomUtil.date()));

            System.out.println("主线程已经结束了");

            executor.shutdown();
        }

    /**
     * 导游线程
     */
    static class TourGuideTask implements Runnable{
        private String [] travels ;

        public TourGuideTask(String...travels) {
            this.travels = travels;
        }

        @Override
        public void run() {
            // 导游线程应该是等大家都到了的时候执行
            System.out.println("导游开始点名,假设点名要 2 秒,这是和大家一起等待的时间");
            try {
                for (String travel : travels) {
                    System.out.print(travel + " ");
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("点名结束");
        }
    }
}
