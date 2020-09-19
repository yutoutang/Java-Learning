## Netty

提高异步的、基于事件驱动的网络应用框架，用以开发高性能、高可靠性的网络IO程序

##### 优点

1. 设计优雅：适用于各种传输类型的统一 API 阻塞和非阻塞 Socket
2. 使用方便：没有其他依赖项
3. 高性能、吞吐量高；延迟更低
4. 安全：SSL/TLS 和 StartTLS
5. 社区活跃



#### 线程模型

1. 传统阻塞I/O服务模型、Reactor模式

2. 根据 Reactor 的数量和处理资源池线程的数量不同：单Reactor单线程、单Reactor多线程、

   主从Reactor多线程 （Netty）



#### 传统的阻塞I/O

1. 采用阻塞I/O模式获取输入的数据
2. 每个连接都需要独立的线程来完成数据的输入，业务处理、数据返回
3. 当并发数很大时，会创建大量的线程，占用较大的系统资源
4. 连接创建后，如果当前线程没有数据可读，该线程会阻塞在 read 操作，造成线程资源的浪费



#### Reactor模式

1. 基于 I/O 复用模型，多个连接公用一个阻塞对象，应用程序只需要在一个阻塞对象等待，无需等待所有连接；
2. 基于线程池复用线程资源：将连接完成后的业务处理分配给线程池，一个线程可以处理多个连接业务

##### 核心

1. Reactor：负责监听和分发事件
2. Handlers：处理程序执行I/O完成业务



#### 单Reactor单线程

1. 实习通过一个阻塞对象处理连接请求
2. Reactor通过Select监听客户端对象，收到事件后进行dispatch
3. 连接请求：accept，业务请求：handler

缺点：单线程，handler只能处理单个业务



#### 单Reactor多线程

handler只做业务请求，将业务处理过程交给worker线程池处理，充分发挥多核CPU

缺点：多线程数据共享，访问复杂，Reactor处理所有的事件的监听和响应，在单线程运行，在高并发场景容易

​			出现性能瓶颈 



#### 主从Reactor多线程

1. Reactor 主线程 MainReactor 对象通过 select 监听连接事件，收到事件后，通过 Acceptor 处理连接事件
2. 当 Acceptor 处理连接事件后，MainReactor 将连接分配给 SubReactor 
3. SubReactor 将连接加入到监听队列进行监听，并创建 handler 进行各种事件处理
4. 当有新事件发生时，SubReactor会调用对应的 handler 处理
5. handler 通过 read 读取数据，分发给后面的 worker 线程池处理，并返回结果
6. handler 收到响应结果后，通过 send 将结果返回给 client
7. MainReactor 可以管理多个 SubReactor



#### Netty 模型

1. Netty 抽象出两组线程池：BossGroup 专门负责接收客户端的连接，workGroup 专门负责网络的读写
2. BossGroup 和 WorkerGroup 类型都是 NioEventLoopGroup
3. NioEventLoopGroup 相当于一个事件循环组，这个组含有多个事件循环，每个循环是 NioEventLoop
4. NioEventLoop 表示一个不断循环执行处理任务的线程，每个 NioEventLoop 都有一个 selector，用于监听socket的网络通讯
5. NioEventLoopGroup 可有多个线程，即含有多个NioEventLoop
6. 每个 BossGroup 下的 NioEventLoop 的执行过程：1）轮询accpet事件 2）处理accpet事件，与client建立连接，生成 NioSocketChannel，并注册到 WorkGroup 中的 NioEventLoop 上的 selector 3）处理任务队列的任务
7. 每个 WorkGroup 下的 NioEventLoop 的执行过程：1）轮询read，write事件 2）处理 I/O 事件，在对应的NioSocketChannel进行处理 3） 处理任务队列的任务
8. 每个 WorkGroup 下的 NioEventLoop 处理业务时，会使用 pipeline （管道），pipeline 中包含 channel，即通过 pipeline 获取到对应的管道



NIOEventLoopGroup 下包含多个 NIOEventLoop

每个 NIOEventLoop 中包含一个 Selector， 一个 TaskQueue

每个 NIOEventLoop 的 Selector 注册多个 NIOChannel

每个 NIOChannel 只绑定一个 NIOEventLoop 

每个 NIOChannel 绑定一个 pipeline



##### 案例

##### 服务端

```java
public class NettyServer {
    public static void main(String[] args) throws InterruptedException {

        // 创建 BossGroup 和 WorkGroup
        // 8 个线程 循环分配
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            // 创建服务器端启动的对象，配置启动参数
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workGroup) // 设置两个线程组
                    .channel(NioServerSocketChannel.class) // 设置服务器通道的实现类
                    .option(ChannelOption.SO_BACKLOG, 128) // 设置线程队列等待连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE, true)  // 设置保持活动连接状态 心跳
                    .childHandler(new ChannelInitializer<>() {
                        // 创建一个通道初始化对象 匿名对象 给 pipeline设置 handler
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    }); // workGroup 的对应的 Event 管道处理器

            System.out.println("Server is ready....");

            // 绑定端口 并且同步处理
            ChannelFuture cf = b.bind(6000).sync();

            // 对关闭通道进行监听
            cf.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
```

##### 服务端handler

```java
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    // 读取客户端发送的消息
    // ChannelHandlerContext：channel pipeline 地址
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("server ctx:" + ctx);
        System.out.println("server thread" + Thread.currentThread().getName());
        Channel channel = ctx.channel();
        ChannelPipeline pipeline = ctx.pipeline(); // 本质是双向链表
        // 将 msg 转 buff(netty)
        ByteBuf buf = (ByteBuf)msg;
        System.out.println("client: "+ ctx.channel().remoteAddress() +"msg:" + buf.toString(CharsetUtil.UTF_8));
    }

    // 数据读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // write + flush 将数据写入到缓冲，并刷新，对发送数据进行编码
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello client", CharsetUtil.UTF_8));
    }

    // 处理异常 关闭通道
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}
```

##### 客户端

```java
public class NettyClient {
    public static void main(String[] args) {
        // 客户端只需要一个 eventLoop
        EventLoopGroup eventExecutors = new NioEventLoopGroup();

        try {
            // 创建启动助手
            Bootstrap b = new Bootstrap();

            b.group(eventExecutors)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyClientHandler());
                        }
                    });

            System.out.println("Client is ok....");
            // 客户端连接 channelFuture 涉及到 netty 的异步模型
            ChannelFuture channelFuture = b.connect("127.0.0.1", 6000).sync();

            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventExecutors.shutdownGracefully();
        }
    }
}
```

##### 客户端handler

```java
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    // 当通道就绪时会触发该方法
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client" + ctx);
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello server", CharsetUtil.UTF_8));
    }

    // 当通道有读取事件时，读取服务端发送的消息
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf)msg;
        System.out.println("server's msg:" + buf.toString(CharsetUtil.UTF_8));
        System.out.println("server is" + ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}
```



#### TaskQueue

1. 用户程序自定义的普通任务
2. 用户自定义定时任务
3. 非当前 Reactor 线程调用 Channel 的各个方法



#### 异步模型

1. 当一个异步过程调用发出，调用者不能立即得到结果，当调用请求在远端完成后，通过状态、通知和回调来通知调用者
2. Netty 的IO 操作是异步的 ，bind、connect、write等操作都是异步操作
3. Netty 利用 Future-Listener 机制
4. Netty 的异步是在建立在 future、callback。future：在调用一个比较耗时的方法 fun 时，不用立即返回 fun 的结果，通过返回一个 future ，后续通过 future 取监控方法 fun 的处理过程（Future-Listener 机制）



#### Future

1. 表示异步的执行结果，可以通过提供的方法来检测执行是否完成
2. ChannelFuture 是一个接口，可以添加一个监听器，调用者通过返回 ChannelFuture 来获取操作执行的状态

好处：异步处理不会造成线程阻塞，线程操作 i/o 时可以执行其他程序





#### ChannelHandler

ChannelInboundHandler 入站处理 I/O

ChannelOutboundHandler 出站处理 I/O



#### Pipeline

ChannelPipeline 是一个 Handler 集合 



#### ChannelHandlerContext

writeAndFlush 写并刷新通道



####  ChannelOption

SO_BACKLOG：对应 TPC/IP 协议 listen 函数中的 backlog 参数，初始化服务器可连接队列大小



#### EventLoopGroup

1. EventLoopGroup 一组 EventLoop 的抽象，netty 工作时会有多个 EventLoop工作，每个 EventLoop 管理一个selector 实例
2. bossGroup （单线程） 负责接受客户端的连接（触发 OP_ACCEPT 事件）并将 SocketChannel 交给 workGroup（多线程 线程数量一般为 cpu 核数的两倍） 轮询进行 （利用 next 选择） I/O 处理



#### UnPooled

##### ByteBuf

```java
public static void main(String[] args) {
    // 创建一个 ByteBuf
    // 1. 创建对象，该对象包含一个数组 arr，是一个 byte[10]
    // 2. netty 的 buf 不需要使用 flip 进行反转 底层了维护 readerIndex （下一个读的位置） 和 writeIndex （下一个写入的位置 i+1）
    // 3. buf 的三个区域 readerIndex writeIndex capacity
    // 0 ---- readerIndex 可读的区域
    // readerIndex --- writeIndex 可写的区域
    // writeIndex --- capacity 容量
    ByteBuf buf = Unpooled.buffer(10);

    for (int i = 0; i < 10; i++) {
        buf.writeByte(i);
    }

    for (int i = 0; i < buf.capacity(); i++) {
        System.out.println(buf.readByte());
    }
}
```

```java
public static void main(String[] args) {
    // ByteBuf api 测试
    ByteBuf buf = Unpooled.copiedBuffer("hello world", StandardCharsets.UTF_8);

    if (buf.hasArray()){
        byte[] content = buf.array();
        System.out.println(buf.capacity()); // 33
        String s = new String(content, StandardCharsets.UTF_8);
        System.out.println(s); // hello world 后面 ASCII 为0的字符也输出了
        System.out.println(buf.getCharSequence(0, buf.writerIndex(), StandardCharsets.UTF_8)); // 推荐这种方式去读 hello world
        System.out.println("bytebuf=" + buf);
        System.out.println(buf.arrayOffset());  // 0
        System.out.println(buf.readerIndex());  // 0
        System.out.println(buf.writerIndex());  // 11
        System.out.println(buf.readableBytes());  // 可读的字节数 = 11
    }
}
```



#### Channel中的msg 

##### 使用 SimpleChannelInboundHandler 来作为 handler 的父类需要注意：

Netty 中 服务端和客户端再调用 writeAndFlush 发送数据时，默认并不是 String 类，需要对通道中的发送数据进行编解码，否则无法接受一端发送数据

```
// 客户端和服务端 都要向 pipeline 中加入 编解码器
pipeline.addLast("decoder", new StringDecoder());
pipeline.addLast("encoder", new StringEncoder());
```



#### 心跳机制

```java
public IdleStateHandler(
        int readerIdleTimeSeconds, // 多长时间没有读，发送心跳检测包检测是否有连接
        int writerIdleTimeSeconds, // 多长时间没有写，发送心跳检测包检测是否有连接
        int allIdleTimeSeconds) // 多长时间没有读、写 {

    this(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds,
         TimeUnit.SECONDS);
}
```



#### WebSocket 长连接

 HttpServerCodec：封装 http 协议

ChunkedWriteHandle、

HttpObjectAggregator：聚合

```java
b.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                // http 协议解析
                pipeline.addLast(new HttpServerCodec());
                // 当以 http 协议发送大量数据时，会产生多次 http 请求，利用 ChunkedWriteHandler HttpObjectAggregator 将请求封装
                pipeline.addLast(new ChunkedWriteHandler());
                pipeline.addLast(new HttpObjectAggregator(8192));
                // http - ws
                pipeline.addLast(new WebSocketServerProtocolHandler("/hello"));
                pipeline.addLast(new MyWebSocketServerHandler());

            }
        });

public class MyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        System.out.println("服务器接受消息" + msg.text());
        ctx.channel().writeAndFlush(new TextWebSocketFrame("[Server]" + LocalDate.now() + ":" + msg.text()));
    }
}
```









#### netty底层查看

strace -ff -o ./ooxx java -jar netty_learning-1.0-SNAPSHOT.jar

netty 4.x 底层使用 epoll 

当客户端连接是

服务端会启动一个线程去接受连接 会分配fd = 7（文件描述符），启动一个线程去处理读/写 fd = 13 （文件描述），fd = 7 和 fd = 13 等待用 epoll_wait，当客户端连接启动一个新的线程和连接socket，fd = 13 的 epoll_wait = 1



```shell
// 正常 netty cpu 1核 4个eventpoll 1个bossgroup 3个workgroup
[root@VM-0-10-centos fd]# ll
total 0
lrwx------ 1 root root 64 Sep  5 18:37 0 -> /dev/pts/0
lrwx------ 1 root root 64 Sep  5 18:37 1 -> /dev/pts/0
lrwx------ 1 root root 64 Sep  5 18:37 10 -> anon_inode:[eventpoll] 1
lr-x------ 1 root root 64 Sep  5 18:37 11 -> pipe:[43867960]
l-wx------ 1 root root 64 Sep  5 18:37 12 -> pipe:[43867960]
lrwx------ 1 root root 64 Sep  5 18:37 13 -> anon_inode:[eventpoll] 2
lr-x------ 1 root root 64 Sep  5 18:37 14 -> pipe:[43867961]
l-wx------ 1 root root 64 Sep  5 18:37 15 -> pipe:[43867961]
lrwx------ 1 root root 64 Sep  5 18:37 16 -> anon_inode:[eventpoll] 3
lrwx------ 1 root root 64 Sep  5 18:37 17 -> socket:[43867962]
lrwx------ 1 root root 64 Sep  5 18:37 18 -> socket:[43867976]
lrwx------ 1 root root 64 Sep  5 18:42 19 -> socket:[43872741]
lrwx------ 1 root root 64 Sep  5 18:37 2 -> /dev/pts/0
lr-x------ 1 root root 64 Sep  5 18:37 3 -> /usr/local/java/jdk1.8.0_221/jre/lib/rt.jar
lr-x------ 1 root root 64 Sep  5 18:37 4 -> /root/io_l/netty_learning-1.0-SNAPSHOT.jar
lr-x------ 1 root root 64 Sep  5 18:37 5 -> pipe:[43867958]
l-wx------ 1 root root 64 Sep  5 18:37 6 -> pipe:[43867958]
lrwx------ 1 root root 64 Sep  5 18:37 7 -> anon_inode:[eventpoll] 4
lr-x------ 1 root root 64 Sep  5 18:37 8 -> pipe:[43867959]
l-wx------ 1 root root 64 Sep  5 18:37 9 -> pipe:[43867959]

workgroup=1（7） bossgroup=1（10） 
2个eventpoll 

root@VM-0-10-centos fd]# ll
total 0
lrwx------ 1 root root 64 Sep  5 18:48 0 -> /dev/pts/0
lrwx------ 1 root root 64 Sep  5 18:48 1 -> /dev/pts/0
lrwx------ 1 root root 64 Sep  5 18:49 10 -> anon_inode:[eventpoll] 1 workgroup
lrwx------ 1 root root 64 Sep  5 18:49 11 -> socket:[43879325]
lrwx------ 1 root root 64 Sep  5 18:49 12 -> socket:[43879338]
lrwx------ 1 root root 64 Sep  5 18:49 2 -> /dev/pts/0
lr-x------ 1 root root 64 Sep  5 18:49 3 -> /usr/local/java/jdk1.8.0_221/jre/lib/rt.jar
lr-x------ 1 root root 64 Sep  5 18:49 4 -> /root/io_l/netty_learning-1.0-SNAPSHOT.jar
lr-x------ 1 root root 64 Sep  5 18:49 5 -> pipe:[43879323]
l-wx------ 1 root root 64 Sep  5 18:49 6 -> pipe:[43879323]
lrwx------ 1 root root 64 Sep  5 18:49 7 -> anon_inode:[eventpoll] 2 bossgroup
lr-x------ 1 root root 64 Sep  5 18:49 8 -> pipe:[43879324]
l-wx------ 1 root root 64 Sep  5 18:49 9 -> pipe:[43879324]

```

