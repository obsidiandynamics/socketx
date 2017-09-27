package com.obsidiandynamics.socketx.undertow;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.*;

import org.junit.*;
import org.junit.rules.*;
import org.xnio.*;

import com.obsidiandynamics.socketx.*;

import io.undertow.connector.*;
import io.undertow.websockets.core.*;
import io.undertow.websockets.extensions.*;

/**
 *  Tests specific to {@link UndertowEndpoint}, beyond what is normally covered by the integration
 *  tests.
 */
public final class UndertowEndpointTest {
  private XEndpointScanner<UndertowEndpoint> scanner;
  private UndertowEndpoint endpoint;
  private XEndpointListener<UndertowEndpoint> listener;
  private MockWebSocketChannel channel;
  
  /**
   *  Subclassed here so that the {@link #isWritesBroken()} method can be mocked without the
   *  normal package visibility constrains.
   */
  private abstract class MockWebSocketChannel extends WebSocketChannel {
    protected MockWebSocketChannel(StreamConnection connectedStreamChannel, ByteBufferPool bufferPool,
                                   WebSocketVersion version, String wsUrl, String subProtocol, boolean client,
                                   boolean extensionsSupported, ExtensionFunction extensionFunction,
                                   Set<WebSocketChannel> peerConnections, OptionMap options) {
      super(connectedStreamChannel, bufferPool, version, wsUrl, subProtocol, client, extensionsSupported, extensionFunction,
            peerConnections, options);
    }
    
    @Override
    protected boolean isWritesBroken() {
      return true;
    }
  }
  
  @Rule 
  public ExpectedException exception = ExpectedException.none();
  
  @After
  public void after() throws InterruptedException, IOException {
    if (scanner != null) scanner.close();
    if (endpoint != null) endpoint.close();
    scanner = null;
    endpoint = null;
  }
  
  @SuppressWarnings("unchecked")
  private void createEndpointManager() {
    listener = mock(XEndpointListener.class);
    scanner = new XEndpointScanner<>(1, 1000);
    final UndertowEndpointManager manager = new UndertowEndpointManager(scanner, 1000, new DerivedEndpointConfig(), listener);
    channel = mock(MockWebSocketChannel.class);
    endpoint = new UndertowEndpoint(manager, channel);
    Assert.assertNotNull(endpoint.getChannel());
  }
  
  @Test
  public void testSendError() throws IOException {
    createEndpointManager();
    
    when(channel.isWritesBroken()).thenReturn(true);
    
    final XSendCallback callback = mock(XSendCallback.class);
    endpoint.send("test", callback);
    verify(callback, times(1)).onError(eq(endpoint), isA(IOException.class));
    assertEquals(0L, endpoint.getBacklog());
    
    endpoint.send("test", null);
    assertEquals(0L, endpoint.getBacklog());
  }
  
  @Test
  public void testOnErrorConnected() {
    createEndpointManager();

    final Throwable cause = new IOException("Boom");
    when(channel.isOpen()).thenReturn(true);
    endpoint.onError(channel, cause);
    verify(listener, times(1)).onError(eq(endpoint), eq(cause));
  }
  
  @Test
  public void testOnErrorDisconnected() {
    createEndpointManager();

    final Throwable cause = new IOException("Boom");
    when(channel.isOpen()).thenReturn(false);
    endpoint.onError(channel, cause);
    verify(listener, times(0)).onError(eq(endpoint), eq(cause));
  }
}
