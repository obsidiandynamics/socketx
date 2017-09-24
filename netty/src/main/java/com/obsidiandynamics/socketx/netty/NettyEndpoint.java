package com.obsidiandynamics.socketx.netty;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.concurrent.atomic.*;

import com.obsidiandynamics.socketx.*;

import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.concurrent.*;

public final class NettyEndpoint implements XEndpoint {
  private final NettyEndpointManager manager;
  private final ChannelHandlerContext handlerContext;
  private final AtomicLong backlog = new AtomicLong();
  private final AtomicBoolean closeFired = new AtomicBoolean();
  
  private volatile Object context;
  
  private volatile long lastActivityTime;

  NettyEndpoint(NettyEndpointManager manager, ChannelHandlerContext handlerContext) {
    this.manager = manager;
    this.handlerContext = handlerContext;
    touchLastActivityTime();
  }
  
  public ChannelHandlerContext getHandlerContext() {
    return handlerContext;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public <T> T getContext() {
    return (T) context;
  }

  @Override
  public void setContext(Object context) {
    this.context = context;
  }
  
  @Override
  public void send(ByteBuffer payload, XSendCallback callback) {
    if (isBelowHWM()) {
      backlog.incrementAndGet();
      final ByteBuf buf = Unpooled.wrappedBuffer(payload);
      final ChannelFuture f = handlerContext.channel().writeAndFlush(new BinaryWebSocketFrame(buf));
      f.addListener(wrapCallback(callback));
      touchLastActivityTime();
    } else if (callback != null) {
      callback.onSkip(this);
    }
  }
  
  @Override
  public void send(String payload, XSendCallback callback) {
    if (isBelowHWM()) {
      backlog.incrementAndGet();
      final ChannelFuture f = handlerContext.channel().writeAndFlush(new TextWebSocketFrame(payload));
      f.addListener(wrapCallback(callback));
      touchLastActivityTime();
    } else if (callback != null) {
      callback.onSkip(this);
    }
  }
  
  private GenericFutureListener<ChannelFuture> wrapCallback(XSendCallback callback) {
    return f -> {
      backlog.decrementAndGet();
      if (callback != null) {
        if (f.isSuccess()) {
          callback.onComplete(this);
        } else {
          callback.onError(this, f.cause());
        }
      }
    };
  }
  
  private boolean isBelowHWM() {
    return backlog.get() < manager.getConfig().highWaterMark;
  }
  
  @Override
  public void sendPing() {
    handlerContext.channel().writeAndFlush(new PingWebSocketFrame());
    touchLastActivityTime();
  }

  @Override
  public boolean isOpen() {
    return handlerContext.channel().isOpen();
  }
  
  @Override
  public void flush() {
    handlerContext.channel().flush();
  }

  @Override
  public void terminate() throws IOException {
    if (handlerContext.channel().isOpen()) {
      handlerContext.channel().close();
    }
    fireCloseEvent();
  }

  @Override
  public void close() throws Exception {
    if (handlerContext.channel().isOpen()) {
      handlerContext.close().get();
    } else {
      fireCloseEvent();
    }
  }
  
  private void fireCloseEvent() {
    if (closeFired.compareAndSet(false, true)) {
      manager.remove(handlerContext.channel().id());
      manager.getListener().onClose(this);
    }
  }
  
  void onBinary(ByteBuffer message) {
    manager.getListener().onBinary(this, message);
    touchLastActivityTime();
  }
  
  void onText(String message) {
    manager.getListener().onText(this, message);
    touchLastActivityTime();
  }
  
  void onPing(ByteBuffer data) {
    manager.getListener().onPing(this, data);
    touchLastActivityTime();
  }
  
  void onPong(ByteBuffer data) {
    manager.getListener().onPong(this, data);
    touchLastActivityTime();
  }
  
  void onDisconnect(int statusCode, String reason) {
    manager.getListener().onDisconnect(this, statusCode, reason);
    touchLastActivityTime();
    fireCloseEvent();
  }
  
  void onError(Throwable cause) {
    if (isOpen()) manager.getListener().onError(this, cause);
  }

  @Override
  public InetSocketAddress getRemoteAddress() {
    return (InetSocketAddress) handlerContext.channel().remoteAddress();
  }

  @Override
  public long getBacklog() {
    return backlog.get();
  }

  @Override
  public long getLastActivityTime() {
    return lastActivityTime;
  }
  
  private void touchLastActivityTime() {
    lastActivityTime = System.currentTimeMillis();
  }

  @Override
  public String toString() {
    return "NettyEndpoint [remote=" + getRemoteAddress() + ", lastActivity=" + getLastActivityZoned() + "]";
  }
}
