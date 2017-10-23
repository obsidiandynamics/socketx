package com.obsidiandynamics.socketx.netty;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.*;

import org.junit.*;
import org.junit.rules.*;

import com.obsidiandynamics.socketx.*;

import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.concurrent.*;

/**
 *  Tests specific to {@link NettyEndpoint}, beyond what is normally covered by the integration
 *  tests.
 */
public final class NettyEndpointTest {
  private XEndpointScanner<NettyEndpoint> scanner;
  private NettyEndpoint endpoint;
  private XEndpointListener<NettyEndpoint> listener;
  private ChannelHandlerContext handlerContext;
  private Channel channel;
  
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
  private void createEndpointManager() {
    listener = mock(XEndpointListener.class);
    scanner = new XEndpointScanner<>(1, 1000);
    final NettyEndpointManager manager = new NettyEndpointManager(scanner, new DerivedEndpointConfig(), listener);
    handlerContext = mock(ChannelHandlerContext.class);
    channel = mock(Channel.class);
    when(handlerContext.channel()).thenReturn(channel);
    when(handlerContext.close()).thenReturn(mock(ChannelFuture.class));
    endpoint = new NettyEndpoint(manager, handlerContext);
    when(channel.id()).thenReturn(new TextChannelId("test"));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testSendError() {
    createEndpointManager();
    assertNotNull(endpoint.getHandlerContext());
    
    final ChannelFuture f = mock(ChannelFuture.class);
    when(channel.writeAndFlush(isA(TextWebSocketFrame.class))).thenReturn(f);
    final Throwable cause = new IOException("Boom");
    when(f.isSuccess()).thenReturn(false);
    when(f.cause()).thenReturn(cause);
    when(f.addListener(notNull())).thenAnswer(invocation -> {
      final GenericFutureListener<ChannelFuture> listener = 
          (GenericFutureListener<ChannelFuture>) invocation.getArguments()[0];
      listener.operationComplete(f);
      return null;
    });
    
    final XSendCallback callback = mock(XSendCallback.class);
    endpoint.send("test", callback);
    verify(callback, times(1)).onError(eq(endpoint), eq(cause));
    assertEquals(0L, endpoint.getBacklog());
    
    endpoint.send("test", null);
    assertEquals(0L, endpoint.getBacklog());
  }
  
  @Test
  public void testOnErrorConnected() {
    createEndpointManager();

    when(channel.isOpen()).thenReturn(true);
    final Throwable cause = new IOException("Boom");
    endpoint.onError(cause);
    verify(listener, times(1)).onError(eq(endpoint), eq(cause));
  }
  
  @Test
  public void testOnErrorDisconnected() {
    createEndpointManager();

    when(channel.isOpen()).thenReturn(false);
    final Throwable cause = new IOException("Boom");
    endpoint.onError(cause);
    verify(listener, times(0)).onError(eq(endpoint), eq(cause));
  }
}
