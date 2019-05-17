package com.netty.nio;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
//该注解表示可以同时被多个channel安全共享使用，保证线程安全
@ChannelHandler.Sharable
/**SimpleChannelInboundHandler会负责释放指向保存该消息的ByteBuf的内存引用，自动释放，支持泛型的消息处理
 *而ChannelInboundHandlerAdapter在其时间节点上不会释放消息，而是将消息传递给下一个ChannelHandler处理。不支持泛型的消息处理
 */
public class ServerHandl extends ChannelInboundHandlerAdapter {
    private static Logger log = LoggerFactory.getLogger(ServerHandl.class.getName());


    /**
     * 客户端连接服务成功
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        InetSocketAddress address= (InetSocketAddress) ctx.channel().remoteAddress();
        address.getPort();
        log.info("服务端：客户端："+address+"连接成功");
        //发送给客户端一条信息
        String str="那边有人吗？有就回应一声，反正我也不会回复你！";
        byte[] bytes=str.getBytes();
        ByteBuf buf= Unpooled.buffer(bytes.length);
        buf.writeBytes(bytes);
        ctx.writeAndFlush(buf);
    }


    /**
     * 服务端读取客户端信息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object arg1)   {
        ByteBuf buf = (ByteBuf) arg1;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        log.info("服务端：读取客户端信息----->："+new String(bytes));
        String str = "你好！ 客户端，我是核武器！";
        byte[] bytes1 =str.getBytes();
        ByteBuf writebuf = Unpooled.buffer(bytes1.length);
        writebuf.writeBytes(bytes1);
        ctx.writeAndFlush(writebuf);


    }
    /**
     * channelRead读取完数据的时候被触发
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)   {
        System.out.println("服务器：读写数据完成");
        //刷新所有挂起到信息
        ctx.flush();
    }

    /**
     * 心跳检测处理器
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)  {

    }

    /**
     * 客户端连接发生异常处理
     */
    @Override
    public void exceptionCaught (ChannelHandlerContext ctx, Throwable cause) {
        log.info("服务端：异常处理---->：" + cause.getMessage()+"---->客户端地址："+ctx.channel().remoteAddress());
        ctx.close();
    }

    /**
     * 客户端断开连接时触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx)   {
        log.info("客户端"+ctx.channel().remoteAddress()+"断开连接！");
    }


}
