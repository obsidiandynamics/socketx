package com.obsidiandynamics.socketx;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.Test;
import org.junit.runner.*;
import org.junit.runners.*;
import org.mockito.*;
import org.slf4j.*;

import com.obsidiandynamics.socketx.jetty.*;
import com.obsidiandynamics.socketx.netty.*;
import com.obsidiandynamics.socketx.ssl.*;
import com.obsidiandynamics.socketx.undertow.*;
import com.obsidiandynamics.socketx.util.*;

import junit.framework.*;

@RunWith(Parameterized.class)
public final class ConnectDisconnectTest extends BaseClientServerTest {
  private static final Logger LOG = Mockito.mock(Logger.class);
  
  private static final boolean HTTP = false;
  private static final boolean HTTPS = true;
  
  private static final int CYCLES = 2;
  private static final int CONNECTIONS = 5;
  private static final int PROGRESS_INTERVAL = 10;
  private static final int MAX_PORT_USE_COUNT = 10_000;
  
  @Parameterized.Parameters
  public static List<Object[]> data() {
    return TestCycle.once();
  }
  
  @Test
  public void testJtJt() throws Exception {
    test(true, CYCLES, CONNECTIONS, HTTP, JettyServer.factory(), JettyClient.factory());
    test(false, CYCLES, CONNECTIONS, HTTP, JettyServer.factory(), JettyClient.factory());
  }

  @Test
  public void testJtJtHttps() throws Exception {
    test(true, CYCLES, CONNECTIONS, HTTPS, JettyServer.factory(), JettyClient.factory());
  }

  @Test
  public void testUtUt() throws Exception {
    test(true, CYCLES, CONNECTIONS, HTTP, UndertowServer.factory(), UndertowClient.factory());
    test(false, CYCLES, CONNECTIONS, HTTP, UndertowServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testUtUtHttps() throws Exception {
    test(true, CYCLES, CONNECTIONS, HTTPS, UndertowServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testNtUt() throws Exception {
    test(true, CYCLES, CONNECTIONS, HTTP, NettyServer.factory(), UndertowClient.factory());
    test(false, CYCLES, CONNECTIONS, HTTP, NettyServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testNtUtHttps() throws Exception {
    test(true, CYCLES, CONNECTIONS, HTTPS, NettyServer.factory(), UndertowClient.factory());
  }

  private void test(boolean clean, int cycles, int connections, boolean https,
                    XServerFactory<? extends XEndpoint> serverFactory,
                    XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    for (int cycle = 0; cycle < cycles; cycle++) {
      if (cycle != 0) init();
      test(clean, connections, https, serverFactory, clientFactory);
      dispose();
      if (PROGRESS_INTERVAL != 0 && cycle % PROGRESS_INTERVAL == PROGRESS_INTERVAL - 1) {
        LOG_STREAM.format("cycle %,d\n", cycle);
      }
    }
  }

  private void test(boolean clean, int connections, boolean https,
                    XServerFactory<? extends XEndpoint> serverFactory,
                    XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    final XServerConfig serverConfig = getDefaultServerConfig(https)
        .withScanInterval(1);
    final Slf4jMockListener serverListener = createSlf4jMockListener(LOG, "s: ");
    doAnswer(invocation -> {
      final XEndpoint endpoint = (XEndpoint) invocation.getArguments()[0];
      endpoint.setContext("testServerContext");
      assertEquals("testServerContext", endpoint.getContext());
      return null;
    }).when(serverListener.mock).onConnect(notNull(XEndpoint.class));
    createServer(serverFactory, serverConfig, serverListener.loggingListener);
    assertNotNull(server.getConfig());

    final XClientConfig clientConfig = getDefaultClientConfig()
        .withScanInterval(1)
        .withSSLContextProvider(CompositeSSLContextProvider.getDevClientDefault());
    createClient(clientFactory, clientConfig);
    assertNotNull(client.getConfig());
    final Slf4jMockListener clientListener = createSlf4jMockListener(LOG, "c: ");
    final List<XEndpoint> endpoints = new ArrayList<>(connections);
    
    // connect all endpoints
    for (int i = 0; i < connections; i++) {
      final int port = https ? serverConfig.httpsPort : serverConfig.port;
      final XEndpoint endpoint = openClientEndpoint(https, port, clientListener.loggingListener);
      endpoint.setContext("testClientContext");
      assertEquals("testClientContext", endpoint.getContext());
      endpoints.add(endpoint);
    }

    // assert connections on server
    SocketUtils.await().until(() -> {
      verify(clientListener.mock, times(connections)).onConnect(notNull(XEndpoint.class));
      verify(serverListener.mock, times(connections)).onConnect(notNull(XEndpoint.class));
    });

    // disconnect all endpoints and await closure
    for (XEndpoint endpoint : endpoints) {
      if (clean) {
        endpoint.close();
        endpoint.close(); // second close() should do no harm, and shouldn't call the handler a second time
      } else {
        endpoint.terminate();
        endpoint.terminate(); // second terminate() should do no harm, and shouldn't call the handler a second time
      }
    }
    
    for (XEndpoint endpoint : endpoints) {
      endpoint.awaitClose(Integer.MAX_VALUE);
    }
    
    // assert disconnections on server
    SocketUtils.await().until(() -> {
      verify(clientListener.mock, times(connections)).onClose(notNull(XEndpoint.class));
      verify(serverListener.mock, times(connections)).onClose(notNull(XEndpoint.class));
      TestCase.assertEquals(0, client.getEndpoints().size());
      TestCase.assertEquals(0, server.getEndpointManager().getEndpoints().size());
    });

    for (XEndpoint endpoint : endpoints) {
      assertNotNull(endpoint.getRemoteAddress());
    }
    
    SocketUtils.drainPort(serverConfig.port, MAX_PORT_USE_COUNT);
  }
}