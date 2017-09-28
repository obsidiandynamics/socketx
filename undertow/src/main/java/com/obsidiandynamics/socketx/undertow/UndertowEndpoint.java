package com.obsidiandynamics.socketx.undertow;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.concurrent.atomic.*;

import com.obsidiandynamics.socketx.*;

import io.undertow.*;
import io.undertow.websockets.core.*;

public final class UndertowEndpoint extends AbstractReceiveListener implements XEndpoint {
  private final UndertowEndpointManager manager;
  
  private final WebSocketChannel channel;
  
  private final AtomicLong backlog = new AtomicLong();
  
  private final AtomicBoolean closeFired = new AtomicBoolean();
  
  private volatile Object context;
  
  private volatile long lastActivityTime;

  UndertowEndpoint(UndertowEndpointManager manager, WebSocketChannel channel) {
    this.manager = manager;
    this.channel = channel;
    touchLastActivityTime();
  }
  
  static UndertowEndpoint clientOf(XEndpointScanner<UndertowEndpoint> scanner, 
                                   WebSocketChannel channel, XClientConfig config, XEndpointListener<? super UndertowEndpoint> listener) {
    return new UndertowEndpointManager(scanner, config.idleTimeoutMillis, config, listener).createEndpoint(channel);
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
  protected void onFullPingMessage(final WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
    super.onFullPingMessage(channel, message);
    touchLastActivityTime();
    final ByteBuffer buf = WebSockets.mergeBuffers(message.getData().getResource());
    manager.getListener().onPing(this, buf);
  }

  @Override
  protected void onFullPongMessage(final WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
    super.onFullPongMessage(channel, message);
    touchLastActivityTime();
    final ByteBuffer buf = WebSockets.mergeBuffers(message.getData().getResource());
    manager.getListener().onPong(this, buf);
  }
  
  @Override
  protected void onFullTextMessage(final WebSocketChannel channel, BufferedTextMessage message) throws IOException {
    manager.getListener().onText(this, message.getData());
    super.onFullTextMessage(channel, message);
    touchLastActivityTime();
  }

  @Override
  protected void onFullBinaryMessage(final WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
    final ByteBuffer buf = WebSockets.mergeBuffers(message.getData().getResource());
    manager.getListener().onBinary(this, buf);
    super.onFullBinaryMessage(channel, message);
    touchLastActivityTime();
  }

  @Override
  protected void onCloseMessage(CloseMessage message, WebSocketChannel channel) {
    super.onCloseMessage(message, channel);
    manager.getListener().onDisconnect(this, message.getCode(), message.getReason());
    channel.addCloseTask(ch -> fireCloseEvent());
    touchLastActivityTime();
  }
  
  @Override
  protected void onError(WebSocketChannel channel, Throwable cause) {
    super.onError(channel, cause);
    if (isOpen()) manager.getListener().onError(this, cause);
  }
  
  @Override
  public void send(String payload, XSendCallback callback) {
    if (isBelowHWM()) {
      backlog.incrementAndGet();
      WebSockets.sendText(payload, channel, wrapCallback(callback));
      touchLastActivityTime();
    } else if (callback != null) {
      callback.onSkip(this);
    }
  }
  
  @Override
  public void send(ByteBuffer payload, XSendCallback callback) {
    if (isBelowHWM()) {
      backlog.incrementAndGet();
      WebSockets.sendBinary(payload, channel, wrapCallback(callback));
      touchLastActivityTime();
    } else if (callback != null) {
      callback.onSkip(this);
    }
  }
  
  WebSocketCallback<Void> wrapCallback(XSendCallback callback) {
    return new WebSocketCallback<Void>() {
      private final AtomicBoolean onceOnly = new AtomicBoolean();
      
      @Override public void complete(WebSocketChannel channel, Void context) {
        if (onceOnly.compareAndSet(false, true)) {
          backlog.decrementAndGet();
          if (callback != null) callback.onComplete(UndertowEndpoint.this);
        }
      }

      @Override public void onError(WebSocketChannel channel, Void context, Throwable cause) {
        if (onceOnly.compareAndSet(false, true)) {
          backlog.decrementAndGet();
          if (callback != null) callback.onError(UndertowEndpoint.this, cause);
        }
      }
    };
  }
  
  private boolean isBelowHWM() {
    return backlog.get() < manager.getConfig().highWaterMark;
  }
  
  public WebSocketChannel getChannel() {
    return channel;
  }
  
  @Override
  public void flush() {
    channel.flush();
  }

  @Override
  public void sendPing() {
    if (channel.isOpen()) {
      touchLastActivityTime();
      WebSockets.sendPing(ByteBuffer.allocate(0), channel, null);
    }
  }

  @Override
  public void close() throws IOException {
    if (channel.isOpen() && ! channel.isCloseFrameSent()) {
      channel.sendClose();
    } else {
      terminate();
    }
  }

  @Override
  public void terminate() throws IOException {
    if (channel.isOpen()) {
      closeChannelAndFireEvent();
    } else {
      fireCloseEvent();
    }
  }
  
  private void closeChannelAndFireEvent() {
    channel.getIoThread().execute(() -> {
      try {
        channel.close();
      } catch (IOException e) {
        final UndertowLogger log = UndertowLogger.ROOT_LOGGER;
        log.ioException(e);
      }
      fireCloseEvent();
    });
  }
  
  private void fireCloseEvent() {
    if (closeFired.compareAndSet(false, true)) {
      manager.remove(this);
      manager.getListener().onClose(this);
    }
  }

  @Override
  public InetSocketAddress getRemoteAddress() {
    return channel.getSourceAddress();
  }

  @Override
  public long getBacklog() {
    return backlog.get();
  }

  @Override
  public boolean isOpen() {
    return channel.isOpen();
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
    return "UndertowEndpoint [remote=" + getRemoteAddress() + ", lastActivity=" + getLastActivityZoned() + "]";
  }
}
