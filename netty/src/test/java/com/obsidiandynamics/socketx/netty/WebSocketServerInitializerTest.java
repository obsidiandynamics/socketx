package com.obsidiandynamics.socketx.netty;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.*;
import java.util.function.*;

import org.junit.*;
import org.junit.rules.*;

import com.obsidiandynamics.socketx.*;

import io.netty.channel.*;
import io.netty.channel.socket.*;
import io.netty.handler.timeout.*;

public final class WebSocketServerInitializerTest {

  
  private XEndpointScanner<NettyEndpoint> scanner;
  private NettyEndpoint endpoint;
  private NettyEndpointManager manager;
  private XEndpointListener<NettyEndpoint> listener;
  private ChannelHandlerContext handlerContext;
  private SocketChannel channel;
  private ChannelPipeline pipeline;
  WebSocketServerInitializer w;
  
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
  }
  
  @Test
  public void testRemoveIdle() throws Exception {
    createFixtures();
    
    testHandler(IdleStateHandler.class, handler -> {
      try {
        final Method m = IdleStateHandler.class.getDeclaredMethod("channelIdle", ChannelHandlerContext.class, IdleStateEvent.class);
        m.setAccessible(true);
        final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(channel);
        m.invoke(handler, ctx, null);
      } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new AssertionError(e);
      }
    });
  }
  
  private <H extends ChannelHandler> void testHandler(Class<H> type, Consumer<? super H> test) throws Exception {
    when(pipeline.addLast(isA(type))).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      final H handler = (H) invocation.getArguments()[0];
      System.out.println("handler=" + handler.getClass());
      test.accept(handler);
      return null;
    });
    w.initChannel(channel);
    verify(pipeline, times(1)).addLast(isA(IdleStateHandler.class));
  }
}
