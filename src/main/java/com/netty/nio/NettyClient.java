package com.netty.nio;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class NettyClient {
    private static Logger log = LoggerFactory.getLogger(ServerHandl.class.getName());
    private String host;
    private int port;

    public  NettyClient(String host,int port){
        this.host=host;
        this.port=port;
        start();
    }
    public  void start() {

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            //创建启动客户端辅助类
            Bootstrap bootstrap = new Bootstrap();
            //把事件生命周期组EventLoopGroup注册引导启动类中去启动
            bootstrap.group(eventLoopGroup);
            //注册channel类型为NioSocketChannel，构造一个Socket工厂
            bootstrap.channel(NioSocketChannel.class);
            //启用心跳保活机制。如果两个小时左右没有任何事件传输，这个机制才会被激活
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            //消息立即发出去，不用等待到一定的数据量才发出去
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.handler(new ClientInitializer());
            //绑定服务器等待绑定完成，调用sync()方法会阻塞直到服务器完成绑定
            Channel ch = bootstrap.connect(host,port).sync().channel();

            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new
                    InputStreamReader(System.in));
            for (;;) {
                log.info("控制台---" + in.readLine());
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                byte[] bytes = line.getBytes();
                ByteBuf buf = Unpooled.buffer(bytes.length);
                buf.writeBytes(bytes);
                ch.writeAndFlush(buf);
            }


        }catch(Exception e){
            log.info("客户端异常------>："+e.getMessage());
        } finally {
            //关闭整个线程组
            eventLoopGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {

        new NettyClient("127.0.0.1",8888);
    }


}
