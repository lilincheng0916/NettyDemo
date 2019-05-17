package com.netty.nio;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {
    private static Logger log = LoggerFactory.getLogger(NettyServer.class.getName());

    private String host;
    private int port;

    public  NettyServer(String host,int port){
        this.host=host;
        this.port=port;
        start();
    }

    //线程总数,拿2倍的和数
    private static final int  BIZGORPUSIZE=Runtime.getRuntime().availableProcessors()*2;
    //线程大小
    private  static final int BIZTHREADSIZE=100;

    //线程组、接受客户端链接. NioEventLoopGroup是一个处理I/O操作的多线程事件循环
    private static final EventLoopGroup  bossGroup=new NioEventLoopGroup(BIZGORPUSIZE);
    //用于处理SocketChannel的数据读写
    private static final EventLoopGroup wordGroup=new NioEventLoopGroup(BIZTHREADSIZE);

    //链接服务器、处理事件（多线程模型）
    public  void start(){
        //创建启动服务辅助类
        ServerBootstrap bootstrap=new ServerBootstrap();
        //把事件生命周期组EventLoopGroup注册引导启动类中去启动
         bootstrap.group(bossGroup,wordGroup);
         //注册channel类型为NioSocketChannel的处理器，，构造一个Socket工厂
         bootstrap.channel(NioServerSocketChannel.class);
        //消息立即发出去，不用等待到一定的数据量才发出去
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        //是否启用心跳保活机制。如果两个小时左右没有任何事件传输，就启用心跳机制，保证连接状态
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
         //设置日志
         bootstrap.handler(new LoggingHandler(LogLevel.INFO));

         //处理accept客户端的channel中的pipeline添加自定义处理函数，handler
         bootstrap.childHandler(  new ServerInitializer());
        try {
            //绑定端口，并同步等待成功，即启动服务端
            ChannelFuture future= bootstrap.bind(host,port).sync();
           //判断当前操作是否成功
           if (future.isDone()){
               log.info("服务端启动成功");
           }
           //监听服务端关闭，并阻塞等待。关闭占位符，而不是关闭管道
            future.channel().closeFuture().sync();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            //关闭线EventLoopGroup对象
            bossGroup.shutdownGracefully();
            wordGroup.shutdownGracefully();
        }
    }


    public static void main(String[] args) {
        new NettyServer("127.0.0.1",8888);
    }
}
