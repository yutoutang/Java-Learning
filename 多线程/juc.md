 ## JUC

Java.util.concurrent

并发：多个线程访问同一个资源

并行：多个事情同时去做 一边一边



AtomicInteger 原子整形



#### volatile

volatile是JVM提供的轻量级的同步机制

特性：保证可见性、不保证原子性、禁止指令重排序

不保证原子性会出现写丢失的情况



#### JMM（java内存模型）

抽象的概念，描述一组规则或规范，同步规定如下：

1. 线程解锁前，必须把共享变量的值刷新回主内存
2. 线程加锁前，必须读取主内存的最新值到自己的工作内存
3. 加、解锁同一把锁

主内存：JMM规定所有变量存储在主内存，共享区域、所有的线程都可以访问，线程对变量的操作在主内存中进行。硬盘<内存<cpu。

java创建对象（Student age=25）至主内存，每个工作线程访问此对象（Student age=25）时，将此对象拷贝至各自线程的工作内存中，某一线程对变量修改（Student age=37）后，需要设计一种机制，通知其余线程此变量已经被修改（Student age=37），并将修改后的拷贝对象写入主内存。

各个线程的工作空间：私有内存，无法相互访问

##### 特性：可见性、原子性、有序性



#### 指令重排序

为了提高性能，编译器和处理器会对指令做重排序

编译器优化重排 -> 指令并行重排 -> 内存系统重排

单线程环境不会发生，处理器在进行重排序时必须考虑指令之间的数据依赖性

多线程中会发生指令重排序

内存屏障（Memoery Barrier），保证特定操作的执行顺序；保证某些变量的内存可见性，

volatile通过插入内存屏障，禁止位于MB上方和下方的指令重排序



#### volatile线程安全保证

解决主内存同步延迟现象导致的可见性问题，禁止指令重排序

```java
// 主内存中 a线程调用addTo60 b线程收到变量被修改的通知
class MyData{ // MyData.java => MyData.class => JVM字节码
    volatile int num = 0;

    public void addTo60(){
        this.num = 60;
    }

    public void addPlus(){
        // num++ 再多线程下是不安全的
        // ++ -> 编译为字节码对应三个指令
        // 获得初始值（getfield）,相加（iadd），写回主内存（putfield）三个操作在多线程抢占时，某些操作会分割
        // 数值小于20000 出现了数值丢失
        num++;
    }

    AtomicInteger atomicInteger = new AtomicInteger();

    public void addPlus2(){
        // 最小单位不可分割
        // CAS操作
        atomicInteger.getAndIncrement();
    }

}

/**
 * 1. 验证volatile的可见性
 *   1.1 num属性不加volatile属性
 *   1.2 num属性添加volatile属性
 * 2. 验证volatile不保证原子性
 *   2.1 原子性：不可分割、完整性，某个线程正在做某个具体业务时，中间不可以被加塞或分割，需要整体同时成功或同时失败
 *   2.2 演示 结果有问题
 *   2.3 解决
 *       * 加sync
 *       * AtomicInteger
 */
public class demo1 {
    // volatile保证原子性，并解决
    private static void setAtomic() {
        MyData myData = new MyData();

        for (int i=1; i <= 20; i++){
            new Thread(()->{
                for (int j = 0; j < 1000 ; j++) {
                    // 不保证原子性
                    myData.addPlus();
                    // 保证原子性
                    myData.addPlus2();
                }
            }, String.valueOf(i)).start();
        }
        // 需要等待20个线程计算完成后，再使用main线程查看结果
        // Thread.activeCount() 当前线程数 默认有两个线程 main + GC
        while (Thread.activeCount() > 2){
            // main线程礼让上面的20个业务线程
            Thread.yield();
        }
        // 最终的计算结果可能不是正确答案  20 * 1000
        System.out.println(Thread.currentThread().getName() + "\t" + "finally number value: " + myData.num);
        System.out.println(Thread.currentThread().getName() + "\t" + "finally number value: " + myData.atomicInteger);
    }

    private static void setOkByVolatile() {
        MyData myData = new MyData();

        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t" + "come in");
            try{
                TimeUnit.SECONDS.sleep(3);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            myData.addTo60();
            System.out.println(Thread.currentThread().getName() + "\t" + "update num value" + myData.num);
        }, "A").start();

        // 第二线程是main线程
        while (myData.num == 0){
            // main线程等待，直到num值不再等于0

        }
        // 如果执行下面代码，则main线程被告知num值被修改
        // 向num添加 volatile 关键字后 main线程被告知
        System.out.println(Thread.currentThread().getName() + "\t" + "mission over");
    }
}

```



#### 单例模式下的线程安全问题

```java
public class SingletonDemo {

    private static SingletonDemo instance = null;

    private SingletonDemo(){
        System.out.println(Thread.currentThread().getName() + "\t" + "构造方法");
    }

    public static SingletonDemo getInstance(){
        if (instance == null){
            instance = new SingletonDemo();
        }
        return instance;
    }

    public static void main(String[] args) {
        // 多线程后，单例模式失效 
        for (int i=1; i <= 10; i++){
            new Thread(SingletonDemo::getInstance, String.valueOf(i)).start();
        }
    }

}
```

##### dcl的单例模式

Dcl(双端检锁机制)机制不一定线程安全，因为有指令重排序的存在。

某一线程执行第一次检查时，读取instance不为null 时，instance的引用对象还没有被初始化

instance初始化添加volatile

```java
// 引入volatile保证DCL单例模式的线程安全
private static volatile SingletonDemo instance = null;

private SingletonDemo(){
}

// DCL（Double Check Lock 双端检锁机制）
public static SingletonDemo getInstance(){
    // 加锁之前和之后双重判断 此外还需要控制指令重排的情况
    if (instance == null){
        synchronized (SingletonDemo.class){
            if (instance == null){
                // memory = allocate() 分配内存空间 1
                // instance(memory) 初始化对象 2
                // instance = memory 设置instance指向的内存地址，此时instance != null 3
                // 语句2和语句3不存在数据依赖关系，语句2和语句3可能发生重排为132的执行顺序，导致instance还没初始化时，就被访问了
                instance = new SingletonDemo();
            }
        }
    }
    return instance;
}
```



### CAS

比较并交换 cpu并发原语

JVM实现CAS的汇编指令，底层操作，由于原语的执行是连续的，所以在执行过程中不会被中断，即不会造成数据不一致，即解决了线程安全问题

CAS有三个操作数，内存V，旧值A，修改更新值B，当 A == V时修改其B，否则不修改

主内存的变量（atomic = 5）被多个线程访问时，被拷贝至各个线程的工作内存中，t1通过CAS（CAS（5，2）如果结果为true）操作修改变量（atomic = 2），并写回主内存并通知其他线程可见，t2通过CAS（CAS（5，2）结果为false），修改失败。

线程中期望值和主物理内存的值相同时，通过CAS可以修改变量的值，否则不能修改

```java
// 主内存中的值为5
AtomicInteger atomicInteger = new AtomicInteger(5);
// mian线程中修改atomicInteger的值 修改成功
System.out.println(atomicInteger.compareAndSet(5, 4) + "\t" + atomicInteger.get());
// 修改失败
System.out.println(atomicInteger.compareAndSet(5, 2000) + "\t" + atomicInteger.get());
```

#### 底层原理

```java
// AtomicInteger 
public class AtomicInteger extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 6214790243416807050L;

    private static final jdk.internal.misc.Unsafe U = jdk.internal.misc.Unsafe.getUnsafe();
    private static final long VALUE = U.objectFieldOffset(AtomicInteger.class, "value");

    private volatile int value;

// CAS i++ 源码
// this：当前对象 value：内存偏移量（内存地址）U：unsafe类
// 
public final int getAndIncrement() {
    return U.getAndAddInt(this, VALUE, 1);
}
// unsafte.class
public native int     getIntVolatile(Object o, long offset);
// 底层操作
// unsafe.getAndAddInt
public final int getAndAddInt(Object o, long offset, int delta) {
  	// o : this，offset: VALUE，delta : 1，v : 修改后的值
    int v;
    do {
      // 通过内存偏移量 修改值
      v = getIntVolatile(o, offset);
    } while (!weakCompareAndSetInt(o, offset, v, v + delta)); // 当期望值和真实值相同时 i++
    return v;
 }


  
```



U:  Unsafe（sun.misc）类时CAS的核心类，通过本地native（调用操作系统底层资源执行任务）方法来访问，类似于C语言中指针直接操做内存地址

VALUE：表示该变量在内存中的偏移地址

CAS的底层实现基于内存地址的偏移（CPU原语）和do/while执行，并没有通过sync（加锁并发性下降）加锁就解决了线程安全问题

#### 操作流程

1. 当前线程A、B同时操作AtomicInteger，AtomicInteger里value的初始值为3（this = 3）,线程A、B在操作时将AtomicInteger的值拷贝至各自工作内存中；
2. 线程A通过getIntVolatil(o ,offet)拿到value为3，A线程被挂起；
3. 线程B通过getIntVolatil(o ,offet)拿到value为3，B线程没有被挂起，并执行CompareAndSetInt，修改值为4，被写入主内存中，AtomicInteger中的value为4；
4. 线程A被唤醒，执行CompareAndSetInt比较，发现自己工作内存中的值3和主内存中的值4，不一致。则本次修改失败，需要再次调用getIntVolatil(o ,offet)；
5. 线程A从新获取value值，由于value被volatile修饰，所有value值在线程B被修改，对线程A拿到值为4，继续执行其业务。

#### CAS缺点

1. 循环时间长（do/while），如果!weakCompareAndSetInt(o, offset, v, v + delta)比较失败，会一直不成功，给CPU带来很大的开销。

2. 只能保证一个变量的原子性操作（this）

3. 引出ABA问题（线程之间业务处理存在时间差）

   CAS算法实现的前提：取出内存中某一时刻的数据并在当下时刻比较并交换，那么这个时间差会导致数据的变化。

   例如线程1、2当前从主内存取值A，由于线程2的业务量小，先将A改为B并写回主内存，发现线程1仍在处理业务，线程2又从主内存取值B，并修改为A写回主内存，此时线程1在进行CAS操作时，发现主内存值为A，线程1修改成功。 （虽然开头和结束的操作成功，但是由于业务时间差，中间过程出现不必要的操作）

   ```java
   static AtomicReference<Integer> atomicReference = new AtomicReference<>(100);
   
   public static void main(String[] args) {
       new Thread(()->{
           atomicReference.compareAndSet(100, 101); // A -> B
           atomicReference.compareAndSet(101, 100); // B -> A
       },"t1").start();
   
       new Thread(()->{
           // 暂停1s，保证线程1完成ABA操作
           try{
               TimeUnit.SECONDS.sleep(1);
           }catch(InterruptedException e){
               e.printStackTrace();
           }
           System.out.println(atomicReference.compareAndSet(100, 2019) + "\t" + atomicReference.get());
       },"t2").start();
       
   }
   ```

#### 解决ABA问题

原子引用：AtomicReference，自定义引用类，执行CAS的类

增加修改版本号（时间戳）

 带时间戳的原子引用：AtomicStampedReference



#### 集合类不安全

##### ArrayList线程不安全

java.util.ConcurrentModificationException 并发修改异常

```java
List<String> list = new ArrayList<>();

for (int i=1; i <= 30; i++){
    new Thread(()->{
        list.add(UUID.randomUUID().toString().substring(0,8));
        System.out.println(list);
    }, String.valueOf(i)).start();
}


```

##### copyOnWriteArrary 向array加入volatile

```java
// copyOnWriteArrary 源
private transient volatile Object[] array;

public boolean add(E e) {
  			// 加锁
        synchronized (lock) {
            Object[] es = getArray();
            int len = es.length;
            es = Arrays.copyOf(es, len + 1); // 复制
            es[len] = e;
            setArray(es); // 写
            return true;
        }
    }
```

写时复制，读写分离的思想

```java
/**
 * ArrayList线程不安全
 * 1. 故障现象
 *      java.util.ConcurrentModificationException
 *
 * 2. 导致原因
 *      并发争抢修改
 *      一个在写，一个在抢占资源，导致数据异常，
 *
 * 3. 解决方法
 *      3.1 Vector 类 加锁 保证数据一致性的同时，降低并发性
 *      3.2 Collections.synchronizedList
 *      3.3 CopyOnWriteArrayList 写时复制
 * 4. 优化建议
 *
 */
public static void main(String[] args) {

    List<String> list = new CopyOnWriteArrayList<>();

    for (int i=1; i <= 30; i++){
        new Thread(()->{
            list.add(UUID.randomUUID().toString().substring(0,8));
            System.out.println(list);
        }, String.valueOf(i)).start();
    }

    // java.util.ConcurrentModificationException
}
```



##### HashSet、HashMap 线程不安全

利用 CopyOnWriteArraySet<>() 解决HashSet

```java
// HashSet底层是HashMap
public HashSet() {
    map = new HashMap<>();
}
// HashSet.add 一个值 只关心 键
// HashMap.put K:V对
public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }
```

利用ConcurrentHashMap<>() （需要看一下源码） 解决HashMap



### Java中的锁

#### 公平锁、非公平锁

公平：先来后到， 多个线程按照申请锁的顺序来获取锁。

非公平：允许加塞，有可能后申请锁的线程先拿到锁，在高并发的情况下，有可能优先级翻转或者饥饿现象

​				优点再于比公平锁吞吐量大



##### ReentrantLock默认是非公平锁

##### Synchronized也是非公平锁



#### ReentrantLock 和 Synchonized 区别

1. ReentrantLock 可以中断的获取锁（lockInterruptibly）
2. ReentrantLock 可尝试非阻塞的获取锁
3. ReentrantLock 可以超时获取锁 （tryLock(timeout, unit)）
4. ReentrantLock 可实现公平锁
5. ReentrantLock 可以绑定多个 Condition 对象



#### 可重入锁（递归锁）

##### ReentrantLock、Synchronized是可重入锁

线程可以进入任何一个它已经拥有的锁所同步的代码块（内层其他方法），外层获得锁，进入内层方法自动获取锁

##### 可重入锁优点：避免死锁

```java
class Phone{
    // sendMs和sendMail拥有同一把锁
    public synchronized void sendMs() throws Exception{
        System.out.println(Thread.currentThread().getId() + "\t invoke sendMs()");
        sendEmail();
    }

    public synchronized void sendEmail() throws Exception{
        System.out.println(Thread.currentThread().getId() + "\t invoke sendEmail()");
    }
  
  	Lock lock = new ReentrantLock();
    @Override
    public void run() {
        get();
    }

    public void get(){
        // lock 和 unlock 需要配对
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getId() + "\t invoke get()");
            set();
        }finally {
            lock.unlock();
        }
    }

    public void set(){
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getId() + "\t invoke set()");
        }finally {
            lock.unlock();
        }
    }
}
/**
 * 1.  synchronized是可重入锁
 * t1	 invoke sendMs()     t1线程在外层方法获取锁
 * t1	 invoke sendEmail()  t1在进入内层方法自动获取锁
 * t2	 invoke sendMs()
 * t2	 invoke sendEmail()
 *
 * 2.  ReentrantLocks是可重入锁
 * t3	 invoke get()
 * t3	 invoke set()
 * t4	 invoke get()
 * t4	 invoke set()
 *
 */
public static void main(String[] args) throws Exception {
        Phone phone = new Phone();

        new Thread(() -> {
            try {
                phone.sendMs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "t1").start();

        new Thread(() -> {
            try {
                phone.sendMs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "t2").start();
  
  			Thread t3 = new Thread(phone);
        Thread t4 = new Thread(phone);

        t3.start();
        t4.start();
    }
```



#### 自旋锁（spinLock）

尝试获取锁的线程不会立即阻塞，而是采用循环的方式尝试获取锁，可以减少上下文切换，但是循环会消耗CPU

不用阻塞、但性能下降

```java
// 原子引用线程
AtomicReference<Thread> atomicReference = new AtomicReference<>();

public void myLock(){
    // 加锁
    // 当前进入线程
    Thread thread = Thread.currentThread();
    System.out.println(thread.getName() + "\t come in");

    // 自旋 while + CAS 多线程中保证当前锁被解锁时，才能锁下一个线程
    while (!atomicReference.compareAndSet(null, thread)){

    }
}

public void myUnLock(){
    // 解锁
    Thread thread = Thread.currentThread();
    atomicReference.compareAndSet(thread, null);
    System.out.println(thread + "\t invoke myUnLock");
}


/**
 * AA   come in
 * BB   come in
 * Thread[AA,5,main]    invoke myUnLock
 * Thread[BB,5,main]    invoke myUnLock
 *
 * A线程加锁 -> 等待5s后解锁
 * A线程加锁1s -> B线程加锁 (B线程需要等到A线程解锁后，才能成功加锁)
 *
 */
public static void main(String[] args) {

    SpinLock spinLock = new SpinLock();

    new Thread(() -> {
        spinLock.myLock();

        try{
            TimeUnit.SECONDS.sleep(5);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        spinLock.myUnLock();
    }, "AA").start();


    try{
        TimeUnit.SECONDS.sleep(1);
    }catch(InterruptedException e){
        e.printStackTrace();
    }

    new Thread(() -> {
        spinLock.myLock();

        try{
            TimeUnit.SECONDS.sleep(1);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        spinLock.myUnLock();
        try{
            TimeUnit.SECONDS.sleep(5);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }, "BB").start();

}
```



#### 读写锁

独占锁：写锁 该锁一次只能被一个线程持有 ReentrantLock、Synchronized

共享锁：读锁 该锁被多个线程持有

互斥锁

##### ReentranReadWriteLock

读锁的共享锁可保证并发读是非常高效的，读读、写读、写写是互斥

```java
/**
 * 多个线程同时读一个资源类没有任何问题，所以为了满足并发量，读取共享资源应该同时进行
 *
 * 但是如果有一个线程去写共享资源，就不应该再其他线程对该资源进行读和写
 *
 * 读 - 读 共存
 * 读 - 写 不共存
 * 写 - 写 不共存
 *
 * 写操作：原子 + 独占 整个过程必须是完整的，中间不能被分割、打断 正在写入1 1写入完成
 *
 * 正确流程：
 * 4    正在写入：4
 * 4    写入完成
 * 5    正在写入：5
 * 5    写入完成
 *
 * 错误流程：
 * 3    正在写入：3
 * 2    正在写入：2
 * 5    正在写入：5
 * 1    正在写入：1
 */

// 资源类
class MyCache{
    // 缓存资源需要用 volatile 修饰 保证可见性
    private volatile Map<String, Object> map = new HashMap<>();
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    // 写
    public void put(String key, Object value){
        rwLock.writeLock().lock();
        try{
            System.out.println(Thread.currentThread().getName() + "\t 正在写入：" + key);

            try{
                TimeUnit.MILLISECONDS.sleep(300);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            map.put(key, value);
            System.out.println(Thread.currentThread().getName() +  "\t 写入完成");
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            rwLock.writeLock().unlock();
        }

    }

    // 读
    public void get(String key){
        rwLock.readLock().lock();
        try{
            System.out.println(Thread.currentThread().getName() + "\t 正在读取");

            try{
                TimeUnit.MILLISECONDS.sleep(300);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            Object o = map.get(key);
            System.out.println(Thread.currentThread().getName() + "\t 读取完成" + o);
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            rwLock.readLock().unlock();
        }
    }
}
public class ReadWriteLockDemo {

    public static void main(String[] args) {
        MyCache myCache = new MyCache();
        for (int i=1; i <= 5; i++){
            final int tempInt = i;
            new Thread(()->{
                // 写
                myCache.put(tempInt+"",tempInt+"");
            }, String.valueOf(i)).start();
        }

        for (int i=1; i <= 5; i++){
            final int tempInt = i;
            new Thread(()->{
                myCache.get(tempInt + "");
            }, String.valueOf(i)).start();
        }
    }
}
```



#### CountDownLatch

线程阻塞到另一系列完成才会被唤醒

倒计时，确保线程执行的顺序正确 减少

```java
private static void closedoor() throws InterruptedException {
    CountDownLatch countDownLatch = new CountDownLatch(6);

    for (int i=1; i <= 6; i++){
        new Thread(()->{
            System.out.println(Thread.currentThread().getName());
            countDownLatch.countDown();
        }, String.valueOf(i)).start();
    }

    // 等待 countDownLatch 减至零
    countDownLatch.await();
    System.out.println(Thread.currentThread().getName());
}
```



#### CyclicBarrier

加法

让一组线程到达一个屏障时被阻塞，直到最后一个线程到达屏障时，屏障才会被解除，所有之前被阻塞的线程被释放。

```java
public static void main(String[] args) {

    CyclicBarrier cyclicBarrier = new CyclicBarrier(7, ()->{
        System.out.println("----召唤神龙");
    });

    for (int i=1; i <= 7; i++){
        final int tempI = i;
        new Thread(()->{
            System.out.println(Thread.currentThread().getName() + "\t 收集到第：" + tempI + "龙珠");
            try {
                // 龙珠没有集齐 无法召唤神龙
                // 内存屏障点
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }, String.valueOf(i)).start();
    }
}
```



#### Semaphore

控制线程的并发数量

控制多个进程对共享资源的访问。

```java
/**
 * 输出：
 * 1   抢到车位
 * 2   抢到车位
 * 3   抢到车位
 * 1   停车3秒后离开车位
 * 4   抢到车位
 * 2   停车3秒后离开车位
 * 5   抢到车位
 * 3   停车3秒后离开车位
 * 6   抢到车位
 * 
 */

public static void main(String[] args) {
    // 三个车位
    Semaphore semaphore = new Semaphore(3);
    // 6 抢 3
    for (int i=1; i <= 6; i++){
        new Thread(()->{
            try {
                // 抢到
                semaphore.acquire();
                System.out.println(Thread.currentThread().getName() + "\t抢到车位");

                try{
                    TimeUnit.SECONDS.sleep(3);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + "\t停车3秒后离开车位");

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // 离开
                semaphore.release();
            }
        }, String.valueOf(i)).start();
    }

```



#### wait、notify、notifyAll

**wait**: 只能在同步方法或同步块中使用 wait。在执行 wait 方法后，当前线程释放锁。调用了 wait 函数的线程会一直等待，直到有其他线程调用了同一个对象的 notify 或 notifyAll 方法才能被唤醒，被唤醒并不代表立刻获得锁，要等待执行 notify 方法的线程执行完，即退出 synchonized 代码块后，当前线程才会释放锁，而呈 wait 状态的线程才能获取该对象锁。

**notify**：随机唤醒一个正在等待的线程；

**notifyAll**：唤醒所有正在等待的线程。



#### ThreadLocal

ThreadLocl 可以实现绑定自己的值，即每个线程有各自独立的副本而互相不受影响。用来解决数据因并非产生不一致的问题（并非解决并发访问共享资源的问题 Semaphore），为每个线程中的并发访问的数据提供一个副本，通过副本来运行业务，导致了内存的消耗，但大大减少了线程同步带来的线程消耗，也减少了并发控制的复杂度

四个方法：get、set、remove、initalValue

```java
// 底层使用 map 确保每次 get 到方法来自同一个线程
public T get() {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null) {
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;
            return result;
        }
    }
    return setInitialValue();
}
```













