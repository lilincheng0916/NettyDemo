package com.netty.nio;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

//该注解表示可以被多个channel同时安全共享使用，保证线程安全
@ChannelHandler.Sharable
/**SimpleChannelInboundHandler会负责释放指向保存该消息的ByteBuf的内存引用，自动释放，支持泛型的消息处理
  *而ChannelInboundHandlerAdapter在其时间节点上不会释放消息，而是将消息传递给下一个ChannelHandler处理。不支持泛型的消息处理
  */
public class ClientHandler extends SimpleChannelInboundHandler<Object> {
    private static Logger log = LoggerFactory.getLogger(ServerHandl.class.getName());

    //零拷贝 --待测试
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object arg1)throws Exception  {
        RandomAccessFile raf=null;
        long length=-1;
        try {
            //打开一个文件
             raf=new RandomAccessFile(arg1.toString(),"r");
            length=raf.length();
        } catch (Exception e) {
            ctx.writeAndFlush( "零拷贝异常："+ e.getClass().getSimpleName() + ":" + e.getMessage());
            return;
        } finally {
            if (length < 0 && raf != null) {
                raf.close();
            }
        }
        ctx.write("第一次拷贝: " + raf.length() );
        if (ctx.pipeline().get(SslHandler.class) == null) {
           //封装成一个 DefaultFileRegion
            ctx.write(new DefaultFileRegion(raf.getChannel(), 0, length));
        } else {
            // 第二次拷贝
            ctx.write(new ChunkedFile(raf));
        }
        ctx.writeAndFlush("拷贝成功");

    }


    /**
     * 客户端连接到服务端
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端传给服务端");
        String str = "你好👋！核武器，我是一只猫！";
        byte[] bytes= str.getBytes();
        ByteBuf buf = Unpooled.buffer(bytes.length);
        buf.writeBytes(bytes);
        ctx.writeAndFlush(buf);
    }


    /**
     * 客户端读取服务端信息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object arg1) throws Exception {
        log.info("客户端开始读取服务端过来的信息");
        ByteBuf buf=(ByteBuf) arg1;
        byte[] bytes=new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        log.info("客户端读取服务端信息："+new String(bytes));
    }



    /**
     * channelRead读取完数据的时候被触发
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)   {
        log.info("客户端：读写数据完成");
        //刷新所有挂起到信息
        ctx.flush();
    }
    /**
     * 空闲时发送心跳检测消息
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)  {
        IdleStateEvent event=(IdleStateEvent)evt;
        String eventType = null;
        switch (event.state()){
            case READER_IDLE:
                eventType = "读空闲";
                break;
            case WRITER_IDLE:
                eventType = "写空闲";
                break;
            case ALL_IDLE:
                eventType ="读写空闲";
                break;
        }
        log.info(ctx.channel().remoteAddress() + "读写超时：" +eventType);
        ctx.channel().close();
    }
    /**
     * 客户端连接发生异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)   {
        log.info("关闭客户端连接");
    }
    /**
     * 服务端断开连接
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx)  {
        log.info("服务端断开连接，开始重新连接");
        //重新连接服务器
        ChannelFuture channelFuture= ctx.channel().closeFuture();
        if (!channelFuture.isSuccess()) {
            final EventLoop loop = channelFuture.channel().eventLoop();
            loop.schedule(new Runnable() {
                @Override
                public void run() {
                    log.info("客户端重新连接: " ,"服务端断开连接，开始重连操作。。。" );
                   // nettyClient.connect(NettyClient.HOST, NettyClient.PORT);

                }
            }, 1L, TimeUnit.SECONDS);
        } else {
            log.info("客户端连接: 连接服务端成功。。。" );
        }
    }


}


//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println("收到服务器端数据："+msg);
//
//        ByteBuf bb = (ByteBuf) msg;
//        byte[] b = new byte[bb.readableBytes()];
//        bb.readBytes(b);
//        System.out.println("收到服务器端数据："+new String(b));
    //}

/*
    // 客户端连接服务器后被调用，并向服务器发送数据
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//
//        log.info("客户端连接服务器，并开始发送数据。。。");
//        //连接成功返回服务端信息，并序列化
//
//        String result="123";
//
//        byte[] req = result.getBytes();
//        ByteBuf buffer = Unpooled.buffer(req.length);
//        buffer.writeBytes(req);
//        //发送数据
//        ctx.writeAndFlush(buffer);
//    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception { // (1)
        log.info("服务器读写数据完成。。。");
//        while (true) {
//            ByteBuf bf = Unpooled.copiedBuffer(("你好服务端：" + Math.random()).getBytes());
//            ctx.writeAndFlush(bf);
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("服务器读写数据完成。。。");

//        ByteBuf m = (ByteBuf) msg;
//        try {
//            long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
//           log.info(String.valueOf(new Date(currentTimeMillis)));
//        } finally {
//            m.release();
//        }
    }
    //接收到服务器发来到数据，处理收到的数据逻辑
//    @Override
//    protected void channelRead0(ChannelHandlerContext arg0, ByteBuf msg)
//            throws Exception {
//        ByteBuf m = (ByteBuf) msg;
//        try {
//            long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
//            System.out.println(new Date(currentTimeMillis));
//        } finally {
//            m.release();
//        }
//        log.info("客户端读取服务端的数据...");
//        byte[] bytes=new byte[msg.readableBytes()];
//        //将msg消息读取到bytes中，并反序列化收到的消息
//        msg.readBytes(bytes);
//        String string = new String(bytes,"UTF-8");
//        log.info("读取的服务端的数据为：" + string);
//
//    }
    // 发生异常时被调用
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        log.info("客户端异常处理。。。");
        ctx.close();//关闭上下文，释放资源

    }*/


