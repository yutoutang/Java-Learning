## IO模型

IO模型：利用什么样的通道进行数据的发送和接受，解决程序通信的性能



BIO：同步并阻塞，服务实现为一个连接一个线程，客户端有连接请求时服务端需要启动一个线程来处理

如果这个连接不做任何事情时，产生不必要的线程开销。（通过线程池改善，实现多个客户端连接服务器）



NIO：同步非阻塞，服务模式一个连接处理多个请求，客户端发送的连接请求都会注册到多路复用器上

（Selector），多路复用器轮询到连接有I/O请求时，才会处理（连接数目较多、连接时间较短）



AIO：异步非阻塞，引入异步通道的概念，采用了 Proactor 模式，有效的请求才启动线程，先由操作系统完成

后才通知服务线程去处理，一般适用于连接较多且连接时间较长的应用（连接数目多、连接比较长）



#### BIO编程

1. 服务端启动 ServerSocket 

2. 客户端启动 Socket 与服务端建立通信

3. 客户端发出请求，先咨询服务端是否有空余线程，如果有响应，则会等待上一次请求结束后再去处理请求，

   如果没有响应，则会等待或拒绝。

##### 服务端编程

```java
public static void handler(Socket socket){
    try {
        System.out.println(Thread.currentThread().getName() + "\t连接");
        byte[] bytes = new byte[1024];
        // 获取输入流 !
        InputStream socketInputStream = socket.getInputStream();
        // 读取客户端发送数据
        while (true){
          	// 没有数据处理时，会阻塞在此处
            int read = socketInputStream.read(bytes);
            if (read != -1){
                System.out.println(new String(bytes, 0, read));
            }
            else {
                break;
            }
        }
    }
    catch (Exception e){
        e.printStackTrace();
    }
    finally {
        System.out.println("关闭client");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public static void main(String[] args) throws IOException {
    // 1. 创建一个线程池
    ExecutorService threadPool = Executors.newCachedThreadPool();

    // 2. 如果有客户端连接就创建一个线程与此通信
    ServerSocket serverSocket = new ServerSocket(2000);

    System.out.println("服务器启动");

    while (true){
        final Socket socket = serverSocket.accept();
        System.out.println("连接一个客户端");

        threadPool.execute(()->{
            handler(socket);
        });
    }
}
```

##### 问题

1. 每个请求都要创建独立的线程，与对应的客户端进行数据 read 、write
2. 当并发量大时，需要创建大量的线程来处理连接
3. 如果当前线程没有需要处理数据时，就会阻塞在 read



#### NIO编程

1. 三大核心：Channel、Buffer、Selector

2. NIO是面向缓冲区，或者面向块的编程，数据读取到一个它稍后处理的缓冲区，需要时可以在缓冲区中前后移

   动，增加了处理过程中的灵活性，使用此可以提供非阻塞的高伸缩性网络

3. NIO用一个线程处理多个操作，10000个请求可以分给50个线程来处理

4. HTTP2.0 使用了多路复用技术



#### NIO和BIO区别

1. BIO是以流的形式存储数据，NIO是以块的方式存储数据

   （BIO中要不是输入流、要不是输出流，NIO中的Buffer可读可写，需要flip切换）

2. BIO是阻塞的，NIO是非阻塞的

3. BIO基于字节流和字符流进行操作，NIO基于Channle、Buffer（双向传递），Selector



#### NIO三大核心的关系

1. 每个Channel都对应一个Buffer
2. 一个Selector对应一个线程
3. 多个Channel 注册到 Selector 中
4. 程序切换到哪个Channel是由事件（Event）决定的
5. Selector根据不同的事件，在各个通道上切换
6. Buffer 是一个内存块， 底层为一个数组
7. 数据的读取写入是通过Buffer，和BIO有着本质不同
8. Channel是双向的，可以返回底层操作系统的情况，Linux底层的操作系统通道就是双向的



#### Buffer

缓冲区：本质上是一个内存块，一个容器对象（含数组），缓冲区对象内置了一些机制，能够跟踪和记录

缓冲区的状态变化情况。Channel提供从文件、网络读取数据的渠道，但是读取和写入数据必须经过Buffer

Channel和Buffer的数据交换是双向的



```java
// buffer 存放在数组中
final int[] hb;                  // Non-null only for heap buffers
```



##### 相关变量

```java
// Invariants: mark <= position <= limit <= capacity
private int mark = -1; 
private int position = 0; // 当前Buffer（数组）的位置
private int limit;  // 缓冲区的终点（数组可以访问的最大下标），不能对缓冲区的极限位置进行读写操作
private int capacity;  // 缓冲区的容量
```



##### 使用

```java
public static void main(String[] args) {
    // 举例说明buffer的使用
    // 创建一个可以存放5个int的Buffer
    IntBuffer intBuffer = IntBuffer.allocate(5);
  	// capactity = 5 limit = 5 position = 0

    // 存buffer
    for (int i = 0; i < intBuffer.capacity(); i++) {
        intBuffer.put(i * 2);
    }
  	// position = 5
    // 取buffer，在取之前需要读写切换(!) 使 position = 0；buffer中的变量可以自行改变
    intBuffer.flip();

    while (intBuffer.hasRemaining()){
        System.out.println(intBuffer.get());
    }
}
```





#### Channel

NIO的通道类似于流，但是有些区别：通道可以同时读写对象、实现异步读写数据、可以读\写缓冲

常用的 Channel ： FileChannel、DatagramChannel、

ServerSocketChannel（服务端）、SockerChannel（客户端）



##### FileChannel

transferFrom：从目标通道中复制数据到当前通道

transferTo：从当前通道复制数据到目标通道



##### 读、写、拷贝案例（read & write）

```java
public static void main(String[] args) throws IOException {
    String str = "hello world";
    // 写操作
    // 1. 创建输出流
    FileOutputStream fos = new FileOutputStream("1.txt");
    // 2. 创建 FileChannel 包装初始的IO流 fileChannel 的真实类型是 FileChannelImpl
    FileChannel fileChannel = fos.getChannel();
    // 3. 创建缓冲区
    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    // 4. 存入数据至缓冲区 以字节形式
    byteBuffer.put(str.getBytes());
    // 5. buffer写入channel 需要进行 flip
    byteBuffer.flip();
    fileChannel.write(byteBuffer);
    // 6. 关闭相关流
    fos.close();
}
```

```java
public static void main(String[] args) throws IOException {
    // 1. 创建文件输入流
    File file = new File("1.txt");
    FileInputStream fis = new FileInputStream(file);

    // 2. 创建输入流通道
    FileChannel channel = fis.getChannel();
    // 3. 创建缓冲区
    ByteBuffer byteBuffer = ByteBuffer.allocate((int) file.length());
    // 4. 读
    channel.read(byteBuffer);
    // 5. 打印
    System.out.println(new String(byteBuffer.array()));

    fis.close();
}
```

```java
public static void main(String[] args) throws IOException {
    FileInputStream fis = new FileInputStream("1.txt");
    FileChannel fisChannel = fis.getChannel();

    FileOutputStream fos = new FileOutputStream("2.txt");
    FileChannel fosChannel = fos.getChannel();

    ByteBuffer byteBuffer = ByteBuffer.allocate(512);

    while (true){
        byteBuffer.clear(); 
        // 一定要 clear 对缓冲区复位 position = 0 否则 position = limit
      	// read = 0 无法判断是否读取完毕
        int read = fisChannel.read(byteBuffer);
				// 判断是否读完
        if (read == -1){
            break;
        }
        byteBuffer.flip();
        fosChannel.write(byteBuffer);
    }

    fis.close();
    fos.close();

}
```

##### 拷贝（TransFrom & TransTo）

```java
public static void main(String[] args) throws IOException {
    FileInputStream fis = new FileInputStream("test.jpg");
    FileChannel fisChannel = fis.getChannel();

    FileOutputStream fos = new FileOutputStream("test1.jpg");
    FileChannel fosChannel = fos.getChannel();

    fosChannel.transferFrom(fisChannel, 0, fisChannel.size());
    
    fis.close();
    fos.close();
}
```



##### mappedByteBuffer

让文件之间在内存（堆外内存）中进行修改，操作系统的操作

```java
public static void main(String[] args) throws IOException {
    RandomAccessFile randomAccessFile = new RandomAccessFile("1.txt", "rw");

    FileChannel fileChannel = randomAccessFile.getChannel();

    // MappedByteBuffer 抽象类
    // FileChannel.MapMode.READ_WRITE：读写模式
    // 0：可以直接修改的起始位置
    // 5：映射到内存的大小 （下标从 0 开始）
    // java.nio.DirectByteBuffer 实现类
    MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 5);

    // IDEA 内部查看没有修改，需要从外部查看 已经修改成功
    map.put(0, (byte)'9');
    map.put(3,(byte)'H');

    randomAccessFile.close();
}
```



##### 分散和聚和

NIO通过多个 Buffer 完成数据的传递

Scattering：将数据写入到 buffer 时，可采用 buffer 数组依次写入

Gathering：从 buffer 读取数据时，可采用 buffer 数组依次读写

```java
public static void main(String[] args) throws IOException {

    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

    InetSocketAddress inetSocketAddress = new InetSocketAddress(7000);
    // 绑定端口 并启动
    serverSocketChannel.socket().bind(inetSocketAddress);

    // 创建 Buffer 数组
    ByteBuffer[] byteBuffers = new ByteBuffer[2];

    byteBuffers[0] = ByteBuffer.allocate(5);
    byteBuffers[1] = ByteBuffer.allocate(3);

    // 等待客户端连接
    SocketChannel socketChannel = serverSocketChannel.accept();

    int messageLen = 8;

    while (true){
        int byteRead = 0;
        while (byteRead < messageLen){
            long read = socketChannel.read(byteBuffers);
            byteRead += read;  // 累计读取个数
            System.out.println("byteRead=" + byteRead);
            // 使用流打印 查看当前 buffer 的pos和limit
            Arrays.stream(byteBuffers).
                    map(buffer -> "position=" + buffer.position() + "\tlimit=" + buffer.limit()).
                    forEach(System.out::println);
        }

        // 将所有 buffer flip
        Arrays.asList(byteBuffers).forEach(ByteBuffer::flip);

        // 将数据读出 显示回客户端
        long byteWrite = 0;
        while (byteWrite < messageLen){
            long write = socketChannel.write(byteBuffers);
            byteWrite += write;
        }

        Arrays.asList(byteBuffers).forEach(ByteBuffer::clear);
        System.out.println("\n");
    }

}
```



### Selector（抽象类）

1. Selector 能够检测多个注册的通道是否有事件发生
2. 线程处理多个请求时，会使用到 Selector
3. 避免多个线程之间的切换



##### open : 创建选择器对象  KQueueSelectorImp（MAC 下的 Selector的实现类）

##### select：阻塞方法，监控所有的Channel，当其中有 IO 操作进行时，将对应的 SelectionKey 加入内部集合

##### selectKeys：拿到所有的 SelectionKey

##### wakeup：唤醒 selector

##### selectNow：非阻塞



#### selectKeys

```
selector.selectedKeys() // 有事件发生的key的数量
selector.keys().size()  // 全部的key的数量
```



##### ServerSocketChannel 在服务端监听新的客户端socket连接，返回SocketChannel

##### SocketChannel 负责服务端具体的读写操作



#### Selector 注册过程

1. 当客户端连接时，通过 ServerSocketChannel 得到 SocketChannel

2. 将 SocketChannel 注册到 Selector 中，register 方法 （传入select监听指定事件），

    一个 Selector 可以注册多个 SokcetChannel

3. 注册后返回 SelectionKey，与 Selector 关联

4. Selector 通过 select 方法，监听通道，返回有事件发生的通道个数

5. 进一步得到 SelectionKey

6. 根据 SelectionKey 发向获取 SocketChannel，完成业务处理

```java
/**
 * NIO 服务端
 */
public class NIOServer {
    public static void main(String[] args) throws IOException {

        // 1. 创建 serverSocketChannel 并绑定为 port
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(6666));
        // 非阻塞模式
        serverSocketChannel.configureBlocking(false);
        // 2. 创建 Selector
        Selector selector = Selector.open();
        // 3. 将 serverSocketChannel 注册到 selector 中， 连接事件 OP_ACCEPT
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // 4. 循环等待客户端连接
        while (true){
            if (selector.select(1000) == 0){
                // 没有事件发生
                System.out.println("服务器等待了 1s 无连接");
                continue;
            }
            // > 0 表示已经获取到关注的事件 并拿到关注事件集合 发向获取通道
            // 拿到有事件发生的 selectionKeys
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            // 遍历 selectionKeys 利用迭代器
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

            while (keyIterator.hasNext()){
                // 获取 selectionKey
                SelectionKey key = keyIterator.next();
                // 通道发生的事件
                if (key.isAcceptable()){
                    // accept 事件 有新的客户端连接 产生新的通道 分配给客户端
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false); // 指定非阻塞
                    System.out.println("客户端连接成功:" + socketChannel.hashCode());
                    // 将 socketChannel
                    // 1. 注册至 selector
                    // 2. 关注事件 OP_READ
                    // 3. 关联一个 Buffer
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }
                if (key.isReadable()){
                    // read 事件
                    // 拿到 socketChannel
                    SocketChannel channel = (SocketChannel) key.channel();
                    // 获取该 channel 关联的 Buffer
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    channel.read(buffer);
                    System.out.println("from 客户端\t" + new String(buffer.array()));
                }
                // 手动从集合中删除当前 key
                keyIterator.remove();
            }
        }
    }
}


/**
 * NIO 客户端
 */
public class NIOClient {
    public static void main(String[] args) throws IOException {
        // 获取通道
        SocketChannel socketChannel = SocketChannel.open();
        // 设置非阻塞
        socketChannel.configureBlocking(false);
        // 提供服务器端的 ip:port
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 6666);

        if (!socketChannel.connect(inetSocketAddress)){
            while (! socketChannel.finishConnect()){
                System.out.println("因为连接需要时间，客户端不会阻塞，可以做其他工作");
            }
        }

        // 连接成功发送数据
        String s = "hello world";
        ByteBuffer byteBuffer = ByteBuffer.wrap(s.getBytes());
        socketChannel.write(byteBuffer);
        System.in.read();
    }
}
```



#### 基于NIO的群聊系统

1. 实现服务器端和客户端的数据简单通信
2. 实现多人群聊
3. 服务端：检测用户上线、离线，并实现消息的转发
4. 客户端：通过channel可无阻塞发送消息给其他用户，同时接受其他用户发送信息



#### 零拷贝

没有 CPU 的拷贝



保护模式

用户态

用户空间

内核空间

切换



#### 计算机BIO底层连接

server / client

阻塞状态

netstat -natp 查看接口使用情况 查看监听进程

cd /proc/进程号

cd fd 查看内容

0、1、2 标准的 I/O 流 文件描述符 连接时socket（返回文件描述符）建立



java 案例

```java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class testS {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8090);
        System.out.println("step 1: new ServerSocket");

        while (true){
            Socket client = serverSocket.accept();  // 底层绑定文件描述符
            System.out.println("step 2: client\t" + client.getPort());
            new Thread(()->{
                try {
                    InputStream ins = client.getInputStream();
                    BufferedReader buf = new BufferedReader(new InputStreamReader(ins));
                    while (true){
                        System.out.println(buf.readLine());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
```

javac 编译为 class文件

利用strace 查看程序进程中的系统调用



strace -ff -o ./ooxx java testS 生成启动线程文件



通过 jps 查看 testS 进程 

```shell
[root@VM-0-10-centos io_l]# jps
26929 Jps
26759 testS
[root@VM-0-10-centos ~]# cd /proc/26759
root@VM-0-10-centos 26759]# cd task  查看进程
[root@VM-0-10-centos task]# ll // JVM 启动过程如下
total 0
dr-xr-xr-x 7 root root 0 Sep  5 15:07 26759
dr-xr-xr-x 7 root root 0 Sep  5 15:07 26760
dr-xr-xr-x 7 root root 0 Sep  5 15:07 26761
dr-xr-xr-x 7 root root 0 Sep  5 15:07 26762
dr-xr-xr-x 7 root root 0 Sep  5 15:07 26763
dr-xr-xr-x 7 root root 0 Sep  5 15:07 26764
dr-xr-xr-x 7 root root 0 Sep  5 15:07 26765
dr-xr-xr-x 7 root root 0 Sep  5 15:07 26766
dr-xr-xr-x 7 root root 0 Sep  5 15:07 26767
dr-xr-xr-x 7 root root 0 Sep  5 15:07 26768
[root@VM-0-10-centos 28321]# cd fd
[root@VM-0-10-centos fd]# ll
total 0
lrwx------ 1 root root 64 Sep  5 15:15 0 -> /dev/pts/0
lrwx------ 1 root root 64 Sep  5 15:15 1 -> /dev/pts/0
lrwx------ 1 root root 64 Sep  5 15:16 2 -> /dev/pts/0
lr-x------ 1 root root 64 Sep  5 15:16 3 -> /usr/local/java/jdk1.8.0_221/jre/lib/rt.jar
lrwx------ 1 root root 64 Sep  5 15:16 4 -> socket:[43703755]
lrwx------ 1 root root 64 Sep  5 15:16 5 -> socket:[43703757]
```

26 行中 5 文件描述符

jvm执行如下语句：

第二个进程文件

```shell
// 系统调用
bind(5, {sa_family=AF_INET6, sin6_port=htons(8090), inet_pton(AF_INET6, "::", &sin6_addr), sin6_flowinfo=0, sin6_scope_id=0}, 28) = 0

poll([{fd=5, events=POLLIN|POLLERR}], 1, -1 // 阻塞


```



客户端连接

```shell
telnet 127.0.0.1 8090

[root@VM-0-10-centos io_l]# strace -ff -o ./ooxx java testS 
step 1: new ServerSocket
step 2: client  44538
```

最后一个进程文件

```shell
recvfrom(6, "hello\r\n", 8192, 0, NULL, NULL) = 7 // 阻塞 解决掉
ioctl(6, FIONREAD, [0])                 = 0  // 响应客户端发送
write(1, "hello", 5)                    = 5
write(1, "\n", 1)                       = 1
recvfrom(6,
```



##### AIO、NIO 程序框架、操作系统

AIO Linux 内核中没有

 

#### NIO问题

问题：每循环一次：有 1000 客户端请求服务端 只有 1 个客户端发送数据 recvfrom 999 次，造成了系统调用浪费 O(1000)

理想：每循环一次：有 1000 客户端请求服务端 只有 1 个客户端发送数据 O(1) ，减少多余的判断，来减少系统调用

解决：多路复用 select 



#### epoll

每循环 O(1) epoll_wait，每一个FD O(1) epoll_ctl 添加到内核

