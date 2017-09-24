package com.obsidiandynamics.socketx.netty;

import java.nio.*;
import java.util.*;
import java.util.concurrent.*;

import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.socket.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.*;
import io.netty.handler.ssl.*;
import io.netty.handler.timeout.*;

final class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
  private final String path;
  private final SslContext sslContext;
  private final NettyEndpointManager manager;
  private final int idleTimeoutMillis;

  WebSocketServerInitializer(NettyEndpointManager manager, String path, 
                             SslContext sslContext, int idleTimeoutMillis) {
    this.manager = manager;
    this.path = path;
    this.sslContext = sslContext;
    this.idleTimeoutMillis = idleTimeoutMillis;
  }

  @Override
  public void initChannel(SocketChannel ch) throws Exception {
    final ChannelPipeline pipeline = ch.pipeline();
    if (sslContext != null) {
      pipeline.addLast(sslContext.newHandler(ch.alloc()));
    }
    pipeline.addLast(new HttpServerCodec());
    pipeline.addLast(new HttpObjectAggregator(65536));
    pipeline.addLast(new IdleStateHandler(0, 0, idleTimeoutMillis, TimeUnit.MILLISECONDS) {
      @Override protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        super.channelIdle(ctx, evt);
        final NettyEndpoint endpoint = manager.remove(ctx.channel().id());
        if (endpoint != null) {
          endpoint.terminate();
        }
      }
    });
    pipeline.addLast(new WebSocketServerCompressionHandler());
    pipeline.addLast(new WebSocketServerProtocolHandler(path, null, true) {
      @Override public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        manager.createEndpoint(ctx);
      }
      
      @Override protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
        super.decode(ctx, frame, out);
        if (frame instanceof CloseWebSocketFrame) {
          final NettyEndpoint endpoint = manager.remove(ctx.channel().id());
          if (endpoint != null) {
            final CloseWebSocketFrame closeFrame = (CloseWebSocketFrame) frame;
            endpoint.onDisconnect(closeFrame.statusCode(), closeFrame.reasonText());
          }
        } else if (frame instanceof TextWebSocketFrame) {
          final NettyEndpoint endpoint = manager.get(ctx.channel().id());
          if (endpoint != null) {
            final TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            endpoint.onText(textFrame.text());
          }
        } else if (frame instanceof BinaryWebSocketFrame) {
          final NettyEndpoint endpoint = manager.get(ctx.channel().id());
          if (endpoint != null) {
            endpoint.onBinary(toByteBuffer(frame.content()));
          }
        } else if (frame instanceof PingWebSocketFrame) {
          final NettyEndpoint endpoint = manager.get(ctx.channel().id());
          if (endpoint != null) {
            endpoint.onPing(toByteBuffer(frame.content()));
          }
        } else if (frame instanceof PongWebSocketFrame) {
          final NettyEndpoint endpoint = manager.get(ctx.channel().id());
          if (endpoint != null) {
            endpoint.onPong(toByteBuffer(frame.content()));
          }
        }
      }
      
      private ByteBuffer toByteBuffer(ByteBuf buf) {
        final ByteBuffer data = ByteBuffer.allocate(buf.readableBytes());
        buf.readBytes(data);
        data.flip();
        return data;
      }
      
      @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        final NettyEndpoint endpoint = manager.get(ctx.channel().id());
        if (endpoint != null) {
          endpoint.onError(cause);
        }
      }
    });
  }
}