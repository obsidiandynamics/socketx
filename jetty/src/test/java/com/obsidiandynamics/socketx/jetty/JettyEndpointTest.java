package com.obsidiandynamics.socketx.jetty;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.nio.*;

import org.eclipse.jetty.websocket.api.*;
import org.hamcrest.*;
import org.junit.*;
import org.junit.rules.*;

import com.obsidiandynamics.socketx.*;

/**
 *  Tests specific to {@link JettyEndpoint}, beyond what is normally covered by the integration
 *  tests.
 */
public final class JettyEndpointTest {
  private XEndpointScanner<JettyEndpoint> scanner;
  private JettyEndpoint endpoint;
  private XEndpointListener<JettyEndpoint> listener;
  
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
  private void createFixtures() {
    listener = mock(XEndpointListener.class);
    scanner = new XEndpointScanner<>(1, 1000);
    final JettyEndpointManager manager = new JettyEndpointManager(scanner, 1000, new DerivedEndpointConfig(), listener);
    endpoint = new JettyEndpoint(manager);
  }
  
  @Test
  public void testSendError() {
    createFixtures();
    
    final Session session = mock(Session.class);
    final RemoteEndpoint remote = mock(RemoteEndpoint.class);
    when(session.getRemote()).thenReturn(remote);
    endpoint.onWebSocketConnect(session);
    
    final Throwable cause = new IOException("Boom");
    doAnswer(invocation -> {
      final WriteCallback writeCallback = (WriteCallback) invocation.getArguments()[1];
      writeCallback.writeFailed(cause);
      return null;
    }).when(remote).sendString(notNull(), notNull());
    
    final XSendCallback callback = mock(XSendCallback.class);
    endpoint.send("test", callback);
    verify(callback, times(1)).onError(eq(endpoint), eq(cause));
    assertEquals(0L, endpoint.getBacklog());
    
    endpoint.send("test", null);
    assertEquals(0L, endpoint.getBacklog());
  }
  
  @Test
  public void testOnWebSocketErrorConnected() {
    createFixtures();

    final Session session = mock(Session.class);
    final RemoteEndpoint remote = mock(RemoteEndpoint.class);
    when(session.getRemote()).thenReturn(remote);
    when(session.isOpen()).thenReturn(true);
    endpoint.onWebSocketConnect(session);

    final Throwable cause = new IOException("Boom");
    endpoint.onWebSocketError(cause);
    verify(listener, times(1)).onError(eq(endpoint), eq(cause));
  }
  
  @Test
  public void testOnWebSocketErrorDisconnected() {
    createFixtures();

    final Session session = mock(Session.class);
    final RemoteEndpoint remote = mock(RemoteEndpoint.class);
    when(session.getRemote()).thenReturn(remote);
    when(session.isOpen()).thenReturn(false);
    endpoint.onWebSocketConnect(session);

    final Throwable cause = new IOException("Boom");
    endpoint.onWebSocketError(cause);
    verify(listener, times(0)).onError(eq(endpoint), eq(cause));
  }
  
  @Test
  public void testSendPingError() throws IOException {
    createFixtures();

    final Session session = mock(Session.class);
    final RemoteEndpoint remote = mock(RemoteEndpoint.class);
    when(session.getRemote()).thenReturn(remote);
    when(session.isOpen()).thenReturn(true);
    endpoint.onWebSocketConnect(session);

    final Throwable cause = new IOException("Boom");
    doThrow(cause).when(remote).sendPing(any(ByteBuffer.class));
    
    exception.expect(RuntimeException.class);
    exception.expectCause(Matchers.equalTo(cause));
    endpoint.sendPing();
  }
  
  @Test
  public void testCloseAndTerminateWithoutSession() throws IOException {
    createFixtures();
    
    endpoint.close();
    endpoint.terminate();
  }
  
  @Test
  public void testCloseAndTerminateWhileClosed() throws IOException {
    createFixtures();
    
    final Session session = mock(Session.class);
    final RemoteEndpoint remote = mock(RemoteEndpoint.class);
    when(session.getRemote()).thenReturn(remote);
    endpoint.onWebSocketConnect(session);
    endpoint.close();
    endpoint.terminate();
  }
  
  @Test
  public void testTerminateWhileOpen() throws IOException {
    createFixtures();
    
    final Session session = mock(Session.class);
    final RemoteEndpoint remote = mock(RemoteEndpoint.class);
    when(session.getRemote()).thenReturn(remote);
    when(session.isOpen()).thenReturn(true);
    endpoint.onWebSocketConnect(session);
    endpoint.terminate();
  }
}
