package com.obsidiandynamics.socketx;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.nio.*;

import org.junit.*;

import com.obsidiandynamics.socketx.jetty.*;
import com.obsidiandynamics.socketx.netty.*;
import com.obsidiandynamics.socketx.undertow.*;
import com.obsidiandynamics.socketx.util.*;

public final class KeepAliveTest extends BaseClientServerTest {
  private static final int CYCLES = 2;
  
  @Test
  public void testJtJtKeepAlive() throws Exception {
    testKeepAlive(CYCLES, JettyServer.factory(), JettyClient.factory());
  }

  @Test
  public void testUtUtKeepAlive() throws Exception {
    testKeepAlive(CYCLES, UndertowServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testNtUtKeepAlive() throws Exception {
    testKeepAlive(CYCLES, NettyServer.factory(), UndertowClient.factory());
  }

  private void testKeepAlive(int cycles,
                             XServerFactory<? extends XEndpoint> serverFactory,
                             XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    for (int cycle = 0; cycle < cycles; cycle++) {
      testKeepAlive(serverFactory, clientFactory);
      dispose();
    }
  }

  private void testKeepAlive(XServerFactory<? extends XEndpoint> serverFactory,
                             XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    final XServerConfig serverConfig = getDefaultServerConfig(false)
        .withScanInterval(1)
        .withPingInterval(1)
        .withIdleTimeout(2000);
    final XEndpointListener<XEndpoint> serverListener = createMockListener();
    createServer(serverFactory, serverConfig, serverListener);

    final XClientConfig clientConfig = getDefaultClientConfig()
        .withScanInterval(1)
        .withIdleTimeout(2000);
    createClient(clientFactory, clientConfig);

    final XEndpointListener<XEndpoint> clientListener = createMockListener();
    openClientEndpoint(false, serverConfig.port, clientListener);
    SocketTestSupport.await().until(() -> {
      verify(serverListener).onConnect(notNull(XEndpoint.class));
      verify(clientListener).onConnect(notNull(XEndpoint.class));
    });
    
    SocketTestSupport.await().until(() -> {
      verify(clientListener, atLeastOnce()).onPing(notNull(XEndpoint.class), notNull(ByteBuffer.class));
      verify(serverListener, atLeastOnce()).onPong(notNull(XEndpoint.class), notNull(ByteBuffer.class));
    });
  }
}