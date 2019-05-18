package com.netty.nio;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.util.concurrent.TimeUnit;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private  static final StringDecoder DECODER=new StringDecoder();
    private  static final StringEncoder ENCODER=new StringEncoder();
    private  static  final ServerHandl SERVER_HANDLER=new ServerHandl();

        @Override
        protected void initChannel(SocketChannel channel) throws Exception {
            /**
             * ChannelPipeline不需要创建，因为ServerBootstrap或Bootstrap启动时，netty会为每个channel链接创建一个独立的pipline管道
             * channelPipeline是Channelhandler的容器，主要负责ChannelHandler管理和事件拦截与调度处理
             * netty中的事件分为inbound和outbound事件，是ChannelPipeline拦截处理事件
             * inbound事件：由I/O线程触发：TCP链路建立事件、链路关闭事件、读事件、异常通知事件等
             * outbound事件：由I/O线程触发：绑定本地地址事件、连接服务端事件、发送事件、刷新、读、断开连接、关闭当前Channel等事件
             */
            ChannelPipeline pipeline= channel.pipeline();
            //将粘包和拆包处理得到的消息转换为字符串,防止粘包
            //pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
            //解码和编码，和客户端保持一致
            //pipeline.addLast(DECODER);
            //pipeline.addLast(ENCODER);
            //处理空闲状态事件的处理器
            //第一个参数是客户端50s时间没有往服务器写数据。
            //第二个参数是服务端70s的时间没有向客户端写数据。
            //第三个客户端没有往服务器端写数据和服务器端没有往客户端写数据100s的时时间
            pipeline.addLast(new IdleStateHandler(50,70,100, TimeUnit.SECONDS));
            pipeline.addLast(SERVER_HANDLER);
            //控制读写入超时时间，如果在指定的时间内都没有数据写或者读，那么就超时。
            pipeline.addLast(new ReadTimeoutHandler(100));//秒
            pipeline.addLast(new WriteTimeoutHandler(100));


        }

}

