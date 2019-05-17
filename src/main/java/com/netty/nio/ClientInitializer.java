package com.netty.nio;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {
    private  static final StringDecoder DECODER=new StringDecoder();
    private  static final StringEncoder ENCODER=new StringEncoder();
    private  static  final ClientHandler CLIENT_HANDLER=new ClientHandler();

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
      ChannelPipeline pipeline= channel.pipeline();
        //将粘包和拆包处理得到的消息转换为字符串,防止粘包
      //  pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        //解码和编码，和服务端保持一致
      //  pipeline.addLast(DECODER);
      //  pipeline.addLast(ENCODER);
        pipeline.addLast(CLIENT_HANDLER);
        //控制读写入超时时间，如果在指定的时间内都没有数据写或者读，那么就超时。
        pipeline.addLast(new ReadTimeoutHandler(100));//秒
        pipeline.addLast(new WriteTimeoutHandler(100));

    }


}
