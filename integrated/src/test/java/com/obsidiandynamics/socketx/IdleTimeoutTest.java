package com.obsidiandynamics.socketx;

import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.mockito.*;

import com.obsidiandynamics.socketx.jetty.*;
import com.obsidiandynamics.socketx.netty.*;
import com.obsidiandynamics.socketx.undertow.*;
import com.obsidiandynamics.socketx.util.*;

@RunWith(Parameterized.class)
public final class IdleTimeoutTest extends BaseClientServerTest {
  private static final int MAX_PORT_USE_COUNT = 10_000;
  
  @Parameterized.Parameters
  public static List<Object[]> data() {
    return TestCycle.once();
  }
  
  @Test
  public void testJtJtServerTimeout() throws Exception {
    // Note: Jetty requires more idle time allowance than others, otherwise the connection
    // times out before it is upgraded to a WebSocket.
    testServerTimeout(JettyServer.factory(), JettyClient.factory(), 500);
  }
  
  @Test
  public void testUtUtServerTimeout() throws Exception {
    testServerTimeout(UndertowServer.factory(), UndertowClient.factory(), 200);
  }
  
  @Test
  public void testNtUtServerTimeout() throws Exception {
    testServerTimeout(NettyServer.factory(), UndertowClient.factory(), 200);
  }
  
  @Test
  public void testJtJtClientTimeout() throws Exception {
    // Note: Jetty requires more idle time allowance than others, otherwise the connection
    // times out before it is upgraded to a WebSocket.
    testClientTimeout(JettyServer.factory(), JettyClient.factory(), 500);
  }
  
  @Test
  public void testUtUtClientTimeout() throws Exception {
    testClientTimeout(UndertowServer.factory(), UndertowClient.factory(), 200);
  }
  
  private void testClientTimeout(XServerFactory<? extends XEndpoint> serverFactory,
                                 XClientFactory<? extends XEndpoint> clientFactory,
                                 int idleTimeoutMillis) throws Exception {
    final XServerConfig serverConfig = getDefaultServerConfig(false)
        .withScanInterval(1);
    final XEndpointListener<XEndpoint> serverListener = createMockListener();
    createServer(serverFactory, serverConfig, serverListener);

    final XClientConfig clientConfig = getDefaultClientConfig()
        .withScanInterval(1)
        .withIdleTimeout(idleTimeoutMillis);
    createClient(clientFactory, clientConfig);

    final XEndpointListener<XEndpoint> clientListener = createMockListener();
    openClientEndpoint(false, serverConfig.port, clientListener);
    SocketTestSupport.await().until(() -> {
      Mockito.verify(serverListener).onConnect(Mockito.notNull(XEndpoint.class));
      Mockito.verify(clientListener).onConnect(Mockito.notNull(XEndpoint.class));
    });
    
    SocketTestSupport.await().until(() -> {
      Mockito.verify(serverListener).onClose(Mockito.notNull(XEndpoint.class));
      Mockito.verify(clientListener).onClose(Mockito.notNull(XEndpoint.class));
    });
  }

  private void testServerTimeout(XServerFactory<? extends XEndpoint> serverFactory,
                                 XClientFactory<? extends XEndpoint> clientFactory,
                                 int idleTimeoutMillis) throws Exception {
    final XServerConfig serverConfig = getDefaultServerConfig(false)
        .withScanInterval(1)
        .withIdleTimeout(idleTimeoutMillis);
    final XEndpointListener<XEndpoint> serverListener = createMockListener();
    createServer(serverFactory, serverConfig, serverListener);

    final XClientConfig clientConfig = getDefaultClientConfig()
        .withScanInterval(1);
    createClient(clientFactory, clientConfig);

    final XEndpointListener<XEndpoint> clientListener = createMockListener();
    openClientEndpoint(false, serverConfig.port, clientListener);
    SocketTestSupport.await().until(() -> {
      Mockito.verify(serverListener).onClose(Mockito.notNull(XEndpoint.class));
      Mockito.verify(clientListener).onClose(Mockito.notNull(XEndpoint.class));
    });
    
    SocketTestSupport.drainPort(serverConfig.port, MAX_PORT_USE_COUNT);
  }
}
