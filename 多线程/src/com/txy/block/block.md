## 阻塞队列

线程1添加元素至阻塞队列，线程2从阻塞队列移除元素 

当阻塞队列是空时，从队列中获取元素的操作将被阻塞

当阻塞队列时满时，往队列里添加元素的操作将被阻塞



从空队列中获取元素的线程将被阻塞（需要添加，解除阻塞），从满队列中添加元素的线程也会被阻塞（需要移除，解除阻塞）



在多线程中：阻塞，在某些情况下会挂起线程，一旦条件满足，被挂起的线程又会被挂起

利用BlockingQueue无需关心线程何时需阻塞或唤醒！

#### 分类

ArrayBlockingQueue：数组结构组成的有界阻塞队列

LinkedBlockingQueue：链表结构组成的有界（Integer.MAX_VALUE 21473467）阻塞队列（不推荐使用）

SynchronousQueue：不存储元素的阻塞队列，也即单个元素的队列（生产一个、消费一个）

#### 方法

##### 抛出异常

当队列满时，继续add，异常 java.lang.IllegalStateException: Queue full

当队列空时，继续remove，异常 java.util.NoSuchElementException

##### 特殊值

offer：插入 成功true，失败false，无异常

poll：  取出 

##### 阻塞

put ： 插入，当队列满时，线程将被阻塞，

take：取出， 当队列空时，线程将被阻塞

##### 超时

offer(e, time, unit)：限定阻塞时间

pull(e, time)



#### SynchronousQueue

每一个put操作，需要等待一个take操作，在能进行下一个put操作



#### 生产者消费者模式

##### 传统版

1.0 sync、wait、nofify

2.0 lock、await、Singal 使用 Condition

一个 **Condition** 和一个 Lock 关联在一起，就像一个条件队列和一个内置锁相关联类似，并提供更丰富的功能：

在每个锁上可存在多个等待、条件等待可以是中断或不中断、基于时限的等待、以及公平的或非公平的队列操作。

```java
/**
 *  一个初始值为零的变量，两个线程对其交替操作，一个加1，一个减1，抢5
 *
 *  1  线程   操作   资源类
 *  2  判断   干活   通知
 *  3  防止虚假唤醒机制 用 while 替换 if
 *  AA	1
 *  BB	0
 *  AA	1
 *  BB	0
 *  AA	1
 *  BB	0
 *  AA	1
 *  BB	0
 *  AA	1
 *  BB	0
 */

class ShareData{
    private int number = 0;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public void increment() throws Exception{
        // 加锁
        lock.lock();
        try{
            // 1. 判断
            while (number != 0){
                // 等待 不能生产
                condition.await();
            }
            // 2. 干活
            number ++;
            System.out.println(Thread.currentThread().getName() + "\t" + number);
            // 3. 通知唤醒
            condition.signalAll();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
    }

    public void decrement() throws Exception{
        // 加锁
        lock.lock();
        try{
            // 1. 判断
            while (number == 0){
                // 等待 不能生产
                condition.await();
            }
            // 2. 干活
            number --;
            System.out.println(Thread.currentThread().getName() + "\t" + number);
            // 3. 通知唤醒
            condition.signalAll();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
    }

}

public class ProdConsumer_Demo {

    public static void main(String[] args) {
        ShareData shareData = new ShareData();

        new Thread(()->{
            for (int i = 0; i < 5; i++) {
                try {
                    shareData.increment();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"AA").start();

        new Thread(()->{
            for (int i = 0; i < 5; i++) {
                try {
                    shareData.decrement();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"BB").start();
    }
}
```



3.0 blockqueue

```java
/**
 * volatile / CAS / atomicInteger / BlockingQueue / 线程交互 / 原子引用
 */

class MyResource{
    // flag 默认开启 进行 生产 + 消费 保证线程的可见性
    private volatile boolean FLAG = true;
    private AtomicInteger atomicInteger = new AtomicInteger();
    BlockingQueue<String> blockingQueue = null;

    public MyResource(BlockingQueue<String> blockingQueue) {
        this.blockingQueue = blockingQueue;
        System.out.println(blockingQueue.getClass().getName());
    }

    public void myProd() throws Exception{
        String data = null;
        boolean retValue;
        while (FLAG){
            data = atomicInteger.incrementAndGet() + "";
            retValue = blockingQueue.offer(data, 2L, TimeUnit.SECONDS);
            // 判断当前线程
            if (retValue){
                System.out.println(Thread.currentThread().getName() + "\t 插入队列" + data + "成功");
            }else {
                System.out.println(Thread.currentThread().getName() + "\t 插入队列" + data + "失败");
            }
            TimeUnit.SECONDS.sleep(1);
        }

        System.out.println(Thread.currentThread().getName() + "\t 停止 FLAG = false 生产工作结束");
    }

    public void myConsumer() throws Exception{
        String res = null;
        while (FLAG){
            res = blockingQueue.poll(2L, TimeUnit.SECONDS);

            if (null == res || "".equalsIgnoreCase(res)){
                FLAG = false;
                System.out.println(Thread.currentThread().getName() + "\t 以超时 消费退出");
                System.out.println();
                System.out.println();
                return;
            }

            System.out.println(Thread.currentThread().getName() + "\t 消费队列" + res + "成功");
        }
    }

    public void stop() throws Exception{
        this.FLAG = false;
    }
}

public class ProdConsumer_BlockingQueueDemo {

    public static void main(String[] args) throws Exception {

        MyResource myResource = new MyResource(new ArrayBlockingQueue<String>(10));

        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t 生产线程启动");
            try {
                myResource.myProd();
            } catch (Exception e) {
                e.printStackTrace();
            }
        },"Prod").start();

        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t 消费线程启动");
            try {
                myResource.myConsumer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        },"Con").start();


        try{
            TimeUnit.SECONDS.sleep(5);
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println();
        System.out.println();
        System.out.println();

        System.out.println("5s时间到, main线程叫停，活动结束");

        myResource.stop();
    }
}
```

#### Synchronized和Lock的区别

##### 1. 原始构成

​	Synchronized是关键字，属于JVM层面

​		monitorenter（底层通过monitor对象完成，wait和notify也是如此）

​	Lock是具体类（java.util.concurrent.locks.Lock）是api层面的锁

```java
public static void main(String[] args) {
        synchronized (new Object()){

        }

        new ReentrantLock();
    }

public static void main(java.lang.String[]);
    Code:
       0: new           #2                  // class java/lang/Object
       3: dup
       4: invokespecial #1                  // Method java/lang/Object."<init>":()V
       7: dup
       8: astore_1
       9: monitorenter
      10: aload_1
      11: monitorexit  // 正常退出
      12: goto          20
      15: astore_2
      16: aload_1
      17: monitorexit  // 异常退出
      18: aload_2
      19: athrow
      20: new           #7                  // class java/util/concurrent/locks/ReentrantLock
      23: dup
      24: invokespecial #9                  // Method java/util/concurrent/locks/ReentrantLock."<init>":()V
      27: pop
      28: return
```

##### 2. 使用方法

​    synchronized 不需要用户手动释放锁，当synchronized代码执行完后系统会自动让线程释放锁的占用

​	RenntrantLock 则需要用户手动释放锁，若没有释放则可能导致死锁现象

```java
Lock lock = new ReentrantLock();
lock.lock();
try{
    // 逻辑
}catch (Exception e){
    e.printStackTrace();
}finally{
    lock.unlock();
}
```

##### 3. 等待是否可中断

​	synchronized 不可中断，除非抛异常或者正常运行完成

​	RenntrantLock 可中断 设置超时方法、lockInterruptibly()

##### 4. 加锁是否公平

​	synchronized  非公平锁

​	RenntrantLock 默认非公平锁，构造方法传入boolean值 可以修改为公平锁

##### 5. 锁绑定多个条件condition

​	synchronized  没有 随机唤醒一个线程或者全部唤醒

​	RenntrantLock 用来实现分组唤醒需要唤醒的线程组，可以精确唤醒



### 线程池

控制运行线程的数量，处理过程中将任务放入队列

线程复用、控制最大并发数、管理线程



Executors 



ThreadPoolExecutor 

```java
// 常用线程池创建，实现ThreadPoolExecutor 
// 由于下面三个线程池的创建方法中使用 
// newFixedThreadPool 和 newSingleThreadExecutor 使用 
// LinkedBlockingQueue （无解阻塞队列 Integer.MAX_VALUE）  
// newCachedThreadPool 使用 Integer.MAX_VALUE 做最大线程数
// Integer.MAX_VALUE 可能会堆积大量的请求，造成 OOM
// 在生产中不用 ！手写线程池
Executors.newFixedThreadPool();  // 一池固定线程 LinkedBlockingQueue
public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }

Executors.newSingleThreadExecutor(); // 一池一线程 LinkedBlockingQueue
public static ExecutorService newSingleThreadExecutor() {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>()));
    }

Executors.newCachedThreadPool();  // 扩容多线程 SynchronousQueue 短期异步的小程序
public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }
```



#### 底层原理

##### 线程池的重要参数

```java
public ThreadPoolExecutor(int corePoolSize,  // 线程池常驻核心线程数
                          int maximumPoolSize, // 线程池最大线程数
                          long keepAliveTime, // 多余线程存活的时间 缩容 大于 corePoolSize 
                          TimeUnit unit,  // keepAliveTime 单位
                          BlockingQueue<Runnable> workQueue, // 任务队列 等待区
                          ThreadFactory threadFactory, // 线程工厂 
                          RejectedExecutionHandler handler // 拒绝策略)
```

##### 工作原理

1. 在创建线程池后，等待提交过来的任务请求

2. 当调用execute方法添加一个请求任务时，线程池会做如下判断：

   2.1 如果正在运行的线程数量小于 corePoolSize ，立刻创建线程执行此任务

   2.2 如果正在运行的线程数量大于 corePoolSize ，则将该任务放入任务队列 workQueue 中

   2.3 如果此时 workQueue 队列已满，但总任务小于 maximumPoolSize ，创建非核心线程执行此任务

   （假设 corePoolSize = 2，maximumPoolSize = 5，有8位消费者依次响应该线程池，消费者消费顺序

   ​	1, 2（2.1）6, 7, 8（2.3）3, 4, 5（2.2））

   2.4 如果此时 workQueue 队列已满，且总任务大于 maximumPoolSize ， 则任务饱和并设置拒绝策略

3. 当一个线程完成任务时，从队列中取下一个任务来执行

4. 当一个线程空闲时间到达 keepAliveTime 时，线程池会判断：线程数大于 corePoolSize ，在该任务被停止

   所有线程池的任务完成后，线程池 size 最终缩到 corePoolSize 

核心线程是否满 -> 阻塞队列是否满 -> 线程池是否满 -> 拒绝策略

线程池最大任务承载量 maximumPoolSize + workQueue.capacity （阻塞队列容量）



#### 线程池的拒绝策略

AbortPolicy：抛出 RejectedExecutionException 异常阻止系统正常运行

CallerRunsPolicy ：不会报异常，将任务回退给调用者，降低新任务的流量 （好用 例如下列代码 ）

DiscardOldestPolicy ：丢弃队列中等待最久的任务

DiscardPolicy：直接丢弃任务

实现 RejectedExecutionHandler 接口



```java
public static void main(String[] args) {
    ExecutorService threadPool = new ThreadPoolExecutor(2, 5,
            1L, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(3), Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy());
  // 线程数大于最大承载数时，线程池将任务回退个main线程

    try {
        for (int i = 0; i < 10; i++) {
            threadPool.execute(()->{
                System.out.println(Thread.currentThread().getName() + "\t 办理业务");
            });

        }
    }catch (Exception e){
        e.printStackTrace();
    }finally {
        threadPool.shutdown();
    }
}
```



#### 如何合理配置线程池数量

##### cpu密集型（计算量）

CPU核数 + 1个线程的线程池 （4核配5）

##### IO密集型（读写 数据库操作）

1. 并不是一直在执行任务 cpu核 x 2
2. 大部分线程阻塞时 cpu核 / 1 - 阻塞系数 (0.8 - 0.9)



#### Java 四种多线程方式

1. 继承Thread
2. 实现Runnable接口（无法回）
3. 实现Callable接口（有返回 利用Futuretask）
4. 使用线程池

```java
// 查看CPU核数
System.out.println(Runtime.getRuntime().availableProcessors());
```





#### Callable

类似于 Runnable 接口，解决 Runnable 接口无法返回值或抛出异常的问题，大部分任务存在延迟计算，例如：数据库查询、从网络上获取资源或某个复杂计算的功能，Callable 接口可以实现。

```java
class MyThread implements Callable<Integer>{

    @Override
    public Integer call() throws Exception {
        System.out.println(Thread.currentThread().getName() + "***** come in callable");

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        return 1024;
    }
}

public class CallableDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {


        // FutureTask 实现 Callable接口
        FutureTask<Integer> futureTask = new FutureTask<>(new MyThread());
//        FutureTask<Integer> futureTask1 = new FutureTask<>(new MyThread());

        Thread t1 = new Thread(futureTask, "AA"); // callable开启线程去计算
        Thread t2 = new Thread(futureTask, "BB"); // 复用AA线程的计算结果 只进去AA线程
        t1.start();
        t2.start();
        System.out.println(Thread.currentThread().getName() + "**************");
        // int result02 = futureTask.get();  阻塞main线程

        int result01 = 100;
        // 要求获得callable的计算结果，如果没有计算完成就去响应，会导致堵塞，直到计算完成
        int result02 = futureTask.get();

        System.out.println(result01 + result02);
    }

}
```



#### 死锁编码及定位分析

两个或以上的进程在执行过程中，因争夺资源而造成的一种互相等待的现象，若无外力干涉他们都会无法推进下去

如果系统资源充足，进程的资源请求都能得到满足，死锁出现的现象可能很低，否则会因争夺有限资源导致死锁

##### 产生原因

系统资源不足、进程运行推进的顺序不合适、资源分配不当

```java
class HoldLockThread implements Runnable{

    private String lockA;
    private String lockB;

    // 持有自己的 还希望索取别人的


    public HoldLockThread(String lockA, String lockB) {
        this.lockA = lockA;
        this.lockB = lockB;
    }

    @Override
    public void run() {
        synchronized (lockA){
            System.out.println(Thread.currentThread().getName() + "\t 自己持有" + lockA + "\t 尝试得到" + lockB);

            try{
                TimeUnit.SECONDS.sleep(2);
            }catch(InterruptedException e){
                e.printStackTrace();
            }

            synchronized (lockB){
                System.out.println(Thread.currentThread().getName() + "\t 自己持有" + lockB + "\t 尝试得到" + lockA);
            }
        }


    }
}

public class DeadLockDemo {

    public static void main(String[] args) {
        String lockA = "lockA";
        String lockB = "lockB";

        new Thread(new HoldLockThread(lockA, lockB), "AAA").start();
        new Thread(new HoldLockThread(lockB, lockA), "BBB").start();
    }

}

// jstack 死锁故障报告
Java stack information for the threads listed above:
===================================================
"AAA":
        at com.txy.lock.HoldLockThread.run(DeadLockDemo.java:34)
        - waiting to lock <0x0000000787e52638> (a java.lang.String)
        - locked <0x0000000787e52608> (a java.lang.String)
        at java.lang.Thread.run(java.base@13-ea/Thread.java:830)
"BBB":
        at com.txy.lock.HoldLockThread.run(DeadLockDemo.java:34)
        - waiting to lock <0x0000000787e52608> (a java.lang.String)
        - locked <0x0000000787e52638> (a java.lang.String)
        at java.lang.Thread.run(java.base@13-ea/Thread.java:830)

Found 1 deadlock.

```

##### 排查错误

jps -l 查看问题java程序进程号

Jstack 问题进程号 



