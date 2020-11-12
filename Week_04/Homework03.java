package java0.conc0303;
import java.util.concurrent.*;

public class Homework03 {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        FiboThread fiboThread = new FiboThread();
        int result = 0;
        // 1. FutureTask直接new Thread
//        FutureTask<Integer> fiboTask = new FutureTask<>(fiboThread);
//        try {
//            new Thread(fiboTask).start();
//            result = fiboTask.get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

        // 2. ExecutorsService
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        try {
//            result = executorService.submit(fiboThread).get();
//            executorService.shutdown();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

        // 3. Semaphore
//        Semaphore semaphore = new Semaphore(0);
//        FiboCounterSemaphore counter = new FiboCounterSemaphore(semaphore);
//        counter.start();
//        try {
//            semaphore.acquire();
//            result = counter.getResult();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        // 4. CountDownLatch
//        CountDownLatch countDownLatch = new CountDownLatch(1);
//        FiboCounterCountDownLatch counter = new FiboCounterCountDownLatch(countDownLatch);
//        counter.start();
//        try {
//            countDownLatch.await();
//            result = counter.getResult();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        //5. CyclicBarrier
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(1, new Runnable() {
            @Override
            public void run() {
                executorService.shutdown();
            }
        });
        FiboCounterCyclicBarrier counter = new FiboCounterCyclicBarrier(cyclicBarrier);
        executorService.submit(counter);
        while (!executorService.isTerminated()) {

        }
        result = counter.getResult();

        System.out.println("异步计算结果为：" + result);
        System.out.println("使用时间：" + (System.currentTimeMillis() - start) + " ms");
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if (a < 2) {
            return 1;
        }
        return fibo(a - 1) + fibo(a - 2);
    }

    static class FiboThread implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            return sum();
        }
    }

    static class FiboCounterSemaphore extends Thread {
        private Semaphore semaphore;
        private int result = 0;

        public FiboCounterSemaphore(Semaphore semaphore) {
            this.semaphore = semaphore;
        }

        @Override
        public void run() {
            result = sum();
            semaphore.release();   // 在子线程里控制释放资源占用
        }

        public int getResult() {
            return result;
        }
    }

    static class FiboCounterCountDownLatch extends Thread {
        private CountDownLatch countDownLatch;
        private int result = 0;

        public FiboCounterCountDownLatch(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            result = sum();
            countDownLatch.countDown();
        }

        public int getResult() {
            return result;
        }
    }

    static class FiboCounterCyclicBarrier implements Runnable {
        private CyclicBarrier cyclicBarrier;
        private int result = 0;

        public FiboCounterCyclicBarrier(CyclicBarrier cyclicBarrier) {
            this.cyclicBarrier = cyclicBarrier;
        }

        @Override
        public void run() {
            result = sum();
            try {
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }

        public int getResult() {
            return result;
        }
    }
}
