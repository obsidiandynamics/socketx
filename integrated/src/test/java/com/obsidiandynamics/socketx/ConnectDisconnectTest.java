package com.obsidiandynamics.socketx;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.Test;
import org.junit.runner.*;
import org.junit.runners.*;
import org.mockito.*;
import org.slf4j.*;

import com.obsidiandynamics.junit.*;
import com.obsidiandynamics.socketx.jetty.*;
import com.obsidiandynamics.socketx.netty.*;
import com.obsidiandynamics.socketx.ssl.*;
import com.obsidiandynamics.socketx.undertow.*;
import com.obsidiandynamics.socketx.util.*;

import junit.framework.*;

@RunWith(Parameterized.class)
public final class ConnectDisconnectTest extends BaseClientServerTest {
  private static final Logger log = Mockito.mock(Logger.class);
  
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

//  @Test
//  public void testUtUtHttpsClientGracefulDisconnect() throws Exception {
//    try {//TODO
//      test(GRACEFUL, CYCLES, CONNECTIONS, HTTPS, CLIENT_DISCONNECT, UndertowServer.factory(), UndertowClient.factory());
//    } catch (Exception e) {
//      System.err.println("TRAPPED");
//      e.printStackTrace();
//      throw e;
//    }
//  }

  @Test
  public void testJtUtHttpsClientGracefulDisconnect() throws Exception {
    try {//TODO
      test(GRACEFUL, CYCLES, CONNECTIONS, HTTPS, CLIENT_DISCONNECT, JettyServer.factory(), UndertowClient.factory());
    } catch (Exception e) {
      System.err.println("TRAPPED");
      e.printStackTrace();
      throw e;
    }
  }
//  
//  @Test
//  public void testUtJtHttpsClientGracefulDisconnect() throws Exception {
//    try {//TODO
//      test(GRACEFUL, CYCLES, CONNECTIONS, HTTPS, CLIENT_DISCONNECT, UndertowServer.factory(), JettyClient.factory());
//    } catch (Exception e) {
//      System.err.println("TRAPPED");
//      e.printStackTrace();
//      throw e;
//    }
//  }

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
      System.out.println("cycle=" + cycle); //TODO
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
    final Slf4jMockListener serverListener = createSlf4jMockListener(log, "s: ");
    doAnswer(invocation -> {
      final XEndpoint endpoint = (XEndpoint) invocation.getArguments()[0];
      endpoint.setContext("testServerContext");
      assertEquals("testServerContext", endpoint.getContext());
      return null;
    }).when(serverListener.mock).onConnect(notNull());
    
    System.out.println("creating server");//TODO
    createServer(serverFactory, serverConfig, serverListener.loggingListener);
    assertNotNull(server.getConfig());

    final XClientConfig clientConfig = getDefaultClientConfig()
        .withScanInterval(1)
        .withSSLContextProvider(CompositeSSLContextProvider.getDevClientDefault());//TODO

    System.out.println("creating client");//TODO
    createClient(clientFactory, clientConfig);
    assertNotNull(client.getConfig());
    final Slf4jMockListener clientListener = createSlf4jMockListener(log, "c: ");
    final List<XEndpoint> endpoints = new ArrayList<>(connections);
    

    System.out.println("connecting endpoints");//TODO
    // connect all endpoints
    for (int i = 0; i < connections; i++) {
      final int port = https ? serverConfig.httpsPort : serverConfig.port;
      System.out.println("port " + port);//TODO
      final XEndpoint endpoint = openClientEndpoint(https, port, clientListener.loggingListener);
      System.out.println("endpoint " + endpoint);//TODO
      endpoint.setContext("testClientContext");
      assertEquals("testClientContext", endpoint.getContext());
      endpoints.add(endpoint);
    }

    System.out.println("asserting connections");//TODO
    // assert connections on server
    SocketUtils.await().until(() -> {
      verify(clientListener.mock, times(connections)).onConnect(notNull());
      verify(serverListener.mock, times(connections)).onConnect(notNull());
    });
    
    final Collection<? extends XEndpoint> toDisconnect = serverDisconnect 
        ? server.getEndpointManager().getEndpoints() : endpoints;


    System.out.println("disconnecting");//TODO
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

    System.out.println("awaiting close");//TODO
    for (XEndpoint endpoint : toDisconnect) {
      endpoint.awaitClose(Integer.MAX_VALUE);
    }

    System.out.println("draining");//TODO
    server.drain();
    client.drain();

    System.out.println("asserting disconnections");//TODO
    // assert disconnections on server
    SocketUtils.await().until(() -> {
      verify(clientListener.mock, times(connections)).onClose(notNull());
      verify(serverListener.mock, times(connections)).onClose(notNull());
      TestCase.assertEquals(0, client.getEndpoints().size());
      TestCase.assertEquals(0, server.getEndpointManager().getEndpoints().size());
    });

    System.out.println("final assertions");//TODO
    for (XEndpoint endpoint : endpoints) {
      // the remote address should still exist, even though the endpoint has been closed
      assertNotNull(endpoint.getRemoteAddress());
      
      // pinging a closed endpoint should do no harm
      endpoint.sendPing();
    }

    System.out.println("drain port");//TODO
    SocketUtils.drainPort(serverConfig.port, MAX_PORT_USE_COUNT);
  }
}