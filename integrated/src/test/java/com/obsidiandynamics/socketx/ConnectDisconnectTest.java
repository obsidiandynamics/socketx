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
  
  private static final boolean ABRUPT = false;
  private static final boolean GRACEFUL = true;
  
  private static final boolean HTTP = false;
  private static final boolean HTTPS = true;
  
  private static final boolean CLIENT_DISCONNECT = false;
  private static final boolean SERVER_DISCONNECT = true;
  
  private static final int CYCLES = 2;
  private static final int CONNECTIONS = 5;
  private static final int PROGRESS_INTERVAL = 10;
  private static final int MAX_PORT_USE_COUNT = 10_000;
  
  @Parameterized.Parameters
  public static List<Object[]> data() {
    return TestCycle.once();
  }
  
  @Test
  public void testJtJtClientGracefulDisconnect() throws Exception {
    test(GRACEFUL, CYCLES, CONNECTIONS, HTTP, CLIENT_DISCONNECT, JettyServer.factory(), JettyClient.factory());
  }
  
  @Test
  public void testJtJtClientAbruptDisconnect() throws Exception {
    test(ABRUPT, CYCLES, CONNECTIONS, HTTP, CLIENT_DISCONNECT, JettyServer.factory(), JettyClient.factory());
  }
  
  @Test
  public void testJtJtServerGracefulDisconnect() throws Exception {
    test(GRACEFUL, CYCLES, CONNECTIONS, HTTP, SERVER_DISCONNECT, JettyServer.factory(), JettyClient.factory());
  }
  
  @Test
  public void testJtJtServerAbruptDisconnect() throws Exception {
    test(ABRUPT, CYCLES, CONNECTIONS, HTTP, SERVER_DISCONNECT, JettyServer.factory(), JettyClient.factory());
  }

  @Test
  public void testJtJtHttpsClientGracefulDisconnect() throws Exception {
    test(GRACEFUL, CYCLES, CONNECTIONS, HTTPS, CLIENT_DISCONNECT, JettyServer.factory(), JettyClient.factory());
  }

  @Test
  public void testUtUtClientGracefulDisconect() throws Exception {
    test(GRACEFUL, CYCLES, CONNECTIONS, HTTP, CLIENT_DISCONNECT, UndertowServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testUtUtClientAbruptDisconect() throws Exception {
    test(ABRUPT, CYCLES, CONNECTIONS, HTTP, CLIENT_DISCONNECT, UndertowServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testUtUtServerGracefulDisconect() throws Exception {
    test(GRACEFUL, CYCLES, CONNECTIONS, HTTP, SERVER_DISCONNECT, UndertowServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testUtUtServerAbruptDisconect() throws Exception {
    test(ABRUPT, CYCLES, CONNECTIONS, HTTP, SERVER_DISCONNECT, UndertowServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testUtUtHttpsClientGracefulDisconnect() throws Exception {
    test(GRACEFUL, CYCLES, CONNECTIONS, HTTPS, CLIENT_DISCONNECT, UndertowServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testNtUtClientGracefulDisconnect() throws Exception {
    test(GRACEFUL, CYCLES, CONNECTIONS, HTTP, CLIENT_DISCONNECT, NettyServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testNtUtClientAbruptDisconnect() throws Exception {
    test(ABRUPT, CYCLES, CONNECTIONS, HTTP, CLIENT_DISCONNECT, NettyServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testNtUtHttpsClientGracefulDisconnect() throws Exception {
    test(GRACEFUL, CYCLES, CONNECTIONS, HTTPS, CLIENT_DISCONNECT, NettyServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testNtUtServerGracefulDisconnect() throws Exception {
    test(GRACEFUL, CYCLES, CONNECTIONS, HTTP, SERVER_DISCONNECT, NettyServer.factory(), UndertowClient.factory());
  }
  @Test
  public void testNtUtServerAbruptDisconnect() throws Exception {
    test(ABRUPT, CYCLES, CONNECTIONS, HTTP, SERVER_DISCONNECT, NettyServer.factory(), UndertowClient.factory());
  }

  private void test(boolean clean, int cycles, int connections, boolean https, boolean serverDisconnect,
                    XServerFactory<? extends XEndpoint> serverFactory,
                    XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    for (int cycle = 0; cycle < cycles; cycle++) {
      if (cycle != 0) init();
      test(clean, connections, https, serverDisconnect, serverFactory, clientFactory);
      dispose();
      if (PROGRESS_INTERVAL != 0 && cycle % PROGRESS_INTERVAL == PROGRESS_INTERVAL - 1) {
        LOG_STREAM.format("cycle %,d\n", cycle);
      }
    }
  }

  private void test(boolean clean, int connections, boolean https, boolean serverDisconnect,
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
    
    final Collection<? extends XEndpoint> toDisconnect = serverDisconnect 
        ? server.getEndpointManager().getEndpoints() : endpoints;

    // disconnect all endpoints and await closure
    for (XEndpoint endpoint : toDisconnect) {
      if (clean) {
        endpoint.close();
        endpoint.close(); // second close() should do no harm, and shouldn't call the handler a second time
      } else {
        endpoint.terminate();
        endpoint.terminate(); // second terminate() should do no harm, and shouldn't call the handler a second time
      }
    }
    
    for (XEndpoint endpoint : toDisconnect) {
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
      // the remote address should still exist, even though the endpoint has been closed
      assertNotNull(endpoint.getRemoteAddress());
      
      // pinging a closed endpoint should do no harm
      endpoint.sendPing();
    }
    
    SocketUtils.drainPort(serverConfig.port, MAX_PORT_USE_COUNT);
  }
}