package com.obsidiandynamics.socketx.netty;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.junit.*;
import org.junit.rules.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.socketx.*;

import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.socket.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.*;
import io.netty.util.*;

public final class WebSocketServerInitializerTest {
  private XEndpointScanner<NettyEndpoint> scanner;
  private NettyEndpoint endpoint;
  private NettyEndpointManager manager;
  private XEndpointListener<NettyEndpoint> listener;
  private ChannelHandlerContext handlerContext;
  private SocketChannel channel;
  private ChannelPipeline pipeline;
  private WebSocketServerInitializer w;
  private ChannelHandlerContext ctx;
  
  @Rule 
  public ExpectedException exception = ExpectedException.none();
  
  @After
  public void after() throws Exception {
    if (scanner != null) scanner.close();
    if (endpoint != null) endpoint.close();
    scanner = null;
    endpoint = null;
  }
  
  private static final class TextChannelId implements ChannelId {
    private static final long serialVersionUID = 1L;

    private final String id;
    
    TextChannelId(String id) {
      this.id = id;
    }

    @Override
    public int compareTo(ChannelId o) {
      return asShortText().compareTo(o.asShortText());
    }

    @Override
    public String asShortText() {
      return id;
    }

    @Override
    public String asLongText() {
      return id;
    }
  }
  
  private static final AttributeKey<WebSocketServerHandshaker> HANDSHAKER_ATTR_KEY =
      AttributeKey.valueOf(WebSocketServerHandshaker.class, "HANDSHAKER");
  
  private static final class ValueAttribute<T> implements Attribute<T> {
    private final AttributeKey<T> key;
    private T value;
    
    ValueAttribute(AttributeKey<T> key, T value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public AttributeKey<T> key() {
      return key;
    }

    @Override
    public T get() {
      return value;
    }

    @Override
    public void set(T value) {
      this.value = value;
    }

    @Override
    public T getAndSet(T value) {
      final T oldValue = this.value;
      this.value = value;
      return oldValue;
    }

    @Override
    public T setIfAbsent(T value) {
      return this.value != null ? getAndSet(value) : this.value;
    }

    @Override
    @Deprecated
    public T getAndRemove() {
      final T oldValue = this.value;
      this.value = null;
      return oldValue;
    }

    @Override
    public boolean compareAndSet(T oldValue, T newValue) {
      if (Objects.equals(oldValue, newValue)) {
        this.value = newValue;
        return true;
      } else {
        return false;
      }
    }

    @Override
    @Deprecated
    public void remove() {
      value = null;
    }
  }
  
  @SuppressWarnings("unchecked")
  private void createFixtures() {
    listener = mock(XEndpointListener.class);
    scanner = new XEndpointScanner<>(1, 1000);
    manager = new NettyEndpointManager(scanner, new DerivedEndpointConfig(), listener);
    handlerContext = mock(ChannelHandlerContext.class);
    channel = mock(SocketChannel.class);
    when(handlerContext.channel()).thenReturn(channel);
    when(handlerContext.close()).thenReturn(mock(ChannelFuture.class));
    endpoint = new NettyEndpoint(manager, handlerContext);
    when(channel.id()).thenReturn(new TextChannelId("test"));
    pipeline = mock(ChannelPipeline.class);
    when(channel.pipeline()).thenReturn(pipeline);
    w = new WebSocketServerInitializer(manager, "/", null, Integer.MAX_VALUE);
    ctx = mock(ChannelHandlerContext.class);
    when(ctx.channel()).thenReturn(channel);
  }
  
  @Test
  public void testRemoveIdle() throws Exception {
    createFixtures();
    
    testHandler(IdleStateHandler.class, handler -> {
      try {
        final Method m = IdleStateHandler.class
            .getDeclaredMethod("channelIdle", ChannelHandlerContext.class, IdleStateEvent.class);
        m.setAccessible(true);
        m.invoke(handler, ctx, null);
      } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new AssertionError(e);
      }
    });
  }
  
  @Test
  public void testDecode() throws Exception {
    createFixtures();
    
    testHandler(WebSocketServerProtocolHandler.class, handler -> {
      try {
        final Method m = WebSocketServerProtocolHandler.class
            .getDeclaredMethod("decode", ChannelHandlerContext.class, WebSocketFrame.class, List.class);
        m.setAccessible(true);
        final WebSocketServerHandshaker handshaker = mock(WebSocketServerHandshaker.class);
        when(channel.attr(eq(HANDSHAKER_ATTR_KEY))).thenReturn(new ValueAttribute<>(HANDSHAKER_ATTR_KEY, handshaker));
        m.invoke(handler, ctx, mock(CloseWebSocketFrame.class), new ArrayList<>());
        m.invoke(handler, ctx, mock(TextWebSocketFrame.class), new ArrayList<>());
        m.invoke(handler, ctx, mock(BinaryWebSocketFrame.class), new ArrayList<>());
        final PingWebSocketFrame pingFrame = mock(PingWebSocketFrame.class);
        when(pingFrame.content()).thenReturn(mock(ByteBuf.class));
        m.invoke(handler, ctx, pingFrame, new ArrayList<>());
        m.invoke(handler, ctx, mock(PongWebSocketFrame.class), new ArrayList<>());
        m.invoke(handler, ctx, mock(WebSocketFrame.class), new ArrayList<>());
      } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new AssertionError(e);
      }
    });
  }
  
  @Test
  public void testExceptionCaught() throws Exception {
    createFixtures();
    
    testHandler(WebSocketServerProtocolHandler.class, handler -> {
      handler.exceptionCaught(ctx, new IOException("boom"));
      manager.createEndpoint(ctx);
      handler.exceptionCaught(ctx, new IOException("boom"));
    });
  }
  
  private <H extends ChannelHandler> void testHandler(Class<H> type, ThrowingConsumer<? super H> test) throws Exception {
    when(pipeline.addLast(isA(type))).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      final H handler = (H) invocation.getArguments()[0];
      test.accept(handler);
      return null;
    });
    w.initChannel(channel);
    verify(pipeline, times(1)).addLast(isA(IdleStateHandler.class));
  }
}
