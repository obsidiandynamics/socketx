package com.obsidiandynamics.socketx;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.nio.*;

import org.junit.*;

import com.obsidiandynamics.socketx.jetty.*;
import com.obsidiandynamics.socketx.netty.*;
import com.obsidiandynamics.socketx.undertow.*;
import com.obsidiandynamics.socketx.util.*;

public final class PingTest extends BaseClientServerTest {
  @Test
  public void testJtJtServerPing() throws Exception {
    testServerPing(JettyServer.factory(), JettyClient.factory());
  }

  @Test
  public void testJtJtClientPing() throws Exception {
    testClientPing(JettyServer.factory(), JettyClient.factory());
  }

  @Test
  public void testUtUtServerPing() throws Exception {
    testServerPing(UndertowServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testUtUtClientPing() throws Exception {
    testClientPing(UndertowServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testNtUtServerPing() throws Exception {
    testServerPing(NettyServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testNtUtClientPing() throws Exception {
    testClientPing(NettyServer.factory(), UndertowClient.factory());
  }
  
  private XServerConfig getServerConfig() {
    return getDefaultServerConfig(false)
        .withScanInterval(1)
        .withPingInterval(Integer.MAX_VALUE)
        .withIdleTimeout(Integer.MAX_VALUE);
  }
  
  private XClientConfig getClientConfig() {
    return getDefaultClientConfig()
        .withScanInterval(1)
        .withIdleTimeout(Integer.MAX_VALUE);
  }
  
  private void awaitConnect(XEndpointListener<XEndpoint> serverListener, XEndpointListener<XEndpoint> clientListener) {
    SocketUtils.await().until(() -> {
      verify(serverListener).onConnect(notNull(XEndpoint.class));
      verify(clientListener).onConnect(notNull(XEndpoint.class));
    });
  }

  private void testServerPing(XServerFactory<? extends XEndpoint> serverFactory,
                              XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    final XEndpointListener<XEndpoint> serverListener = createMockListener();
    createServer(serverFactory, getServerConfig(), serverListener);
    createClient(clientFactory, getClientConfig());

    final XEndpointListener<XEndpoint> clientListener = createMockListener();
    openClientEndpoint(false, server.getConfig().port, clientListener);
    awaitConnect(serverListener, clientListener);

    final XEndpoint serverEndpoint = server.getEndpointManager().getEndpoints().iterator().next();
    serverEndpoint.sendPing();
    
    SocketUtils.await().until(() -> {
      verify(clientListener, times(1)).onPing(notNull(XEndpoint.class), notNull(ByteBuffer.class));
      verify(serverListener, times(1)).onPong(notNull(XEndpoint.class), notNull(ByteBuffer.class));
    });
  }

  private void testClientPing(XServerFactory<? extends XEndpoint> serverFactory,
                              XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    final XEndpointListener<XEndpoint> serverListener = createMockListener();
    createServer(serverFactory, getServerConfig(), serverListener);
    createClient(clientFactory, getClientConfig());

    final XEndpointListener<XEndpoint> clientListener = createMockListener();
    final XEndpoint clientEndpoint = openClientEndpoint(false, server.getConfig().port, clientListener);
    awaitConnect(serverListener, clientListener);

    clientEndpoint.sendPing();
    
    SocketUtils.await().until(() -> {
      verify(serverListener, times(1)).onPing(notNull(XEndpoint.class), notNull(ByteBuffer.class));
      verify(clientListener, times(1)).onPong(notNull(XEndpoint.class), notNull(ByteBuffer.class));
    });
  }
}