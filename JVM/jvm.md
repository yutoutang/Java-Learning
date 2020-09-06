## JVM&GC

JVM是运行在操作系统之上，它与硬件没有直接的交互



#### 类装载器

负责加载class文件，class文件在文件开头有特定的文件标识（cafe babe），将class字节码内容加载到内存，并

将内容转换成方法区中的运行时数据结构，ClassLoader只负责class文件的加载

虚拟机自带加载器

启动类加载器 （Bootstrap）object string utils.ArrayList等来自 rt.jar

扩展类加载器（Extension）javax

应用程序类加载器

```java
public static void main(String[] args) {
    Object object = new Object();

    System.out.println(object.getClass().getClassLoader()); // null Bootstrap

    MyObject myObject = new MyObject();

    System.out.println(myObject.getClass().getClassLoader()); // jdk.internal.loader.ClassLoaders$AppClassLoader@3d4eac69 应用程序类加载器
}
```

##### 双亲委派

java的类加载器，从上到下开始加载类，

假设本地创建 java.lang.String.main 在运行代码时，java会从rt.jar包开始加载，找到String类并开始加载，而不

会加载本地创建的 java.lang.String

优点：不同的本地类，引用Object时，保证加载相同的Object类



#### Native

```java
// java 线程 start 方法
public synchronized void start() {
        if (threadStatus != 0)
            throw new IllegalThreadStateException();
            
        group.add(this);
        boolean started = false;
        try {
            start0();
            started = true;
        } finally {
            try {
                if (!started) {
                    group.threadStartFailed(this);
                }
            } catch (Throwable ignore) {
   
            }
        }
    }
// java 调用底层 操作系统 或 C 的方法
private native void start0();
```



#### 程序计数器（PC寄存器）

每个线程都有程序计数器，是线程私有的，就是一个指针指向下一条指令的地址



#### 方法区

供线程共享的内存区域，存储类的结构信息

方法区就是一个规范，在不同的虚拟机里实现不同，例如永久带（PermGen space）和原空间（MetaSpace）

实例变量存在堆内存中和方法区无关



栈管运行、堆管存储

#### 栈

主管程序的运行，在线程创建时被创建，其生命周期就是线程的生命周期，不存在垃圾回收问题

栈保存8种基本类型 + 对象的引用变量 + 实例方法

```java
public static void m1(){
    m1();
}

// java.lang.StackOverflowError 栈溢出 是错误不是异常 VirtualMachineError
public static void main(String[] args) {
    System.out.println("!111");
    m1();
    System.out.println("222");
}
```

栈 -> 堆 -> 方法区



#### 堆

类加载器读取类文件后，把类、方法、常变量放到堆内存区

堆内存逻辑上分为新生区（Eden Space（80%）、From Survivor（10%）、To Survivor（10%））（1/3）、

养老区（2/3）、元空间（java7之前为永久区）

Eden（new 对象） 满了 开启 Young GC，Eden基本全部清空

From Survivor 区和 To Survivor区在每次CG会交换，谁空谁是To Survivor

Old 满了 开启 Full GC ，Full GC 多次，Old 空间无法下降，OOM异常（堆内存溢出）



#### Minor GC

复制：eden、from 复制到 to，年龄 + 1

清空：清空 eden、from，

互换：谁空谁是to form -> to



#### 堆参数调优

JVM Heap：-Xms 初始化大小  -Xmx 最大（young + old） 

Young Gen：-Xmn（young）

 -Xms：默认物理内存1/64

-Xmx：默认物理内存 1/4



```java
// java.lang.OutOfMemoryError: Java heap space
// -Xms10m -Xmx10m -XX:+PrintGCDetails
public static void main(String[] args) {
        String str = "A";
        while (true){
            str += str + new Random().nextInt(88888);
        }

}
```



#### GC作用域&算法

方法区和堆

1. 引用计数
2. 标记清除：标记出回收对象，然后统一回收这些对象，容易产生内存碎片（内存资源分布较散）
3. 复制：复制之后有交换、谁空谁是to
4. 标记整理：标记、回收、整理 （耗时长）



#### GC Root

JVM回收的垃圾：内存中已经不在被使用的空间就是垃圾

如何判读：

1. 引用计数法（很难解决对象之间的循环引用问题）
2. 枚举根节点做可达性分析：GC root 是一组活跃的引用

