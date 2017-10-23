package com.obsidiandynamics.socketx;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.*;
import java.util.*;

import org.junit.*;

import com.obsidiandynamics.socketx.jetty.*;
import com.obsidiandynamics.socketx.netty.*;
import com.obsidiandynamics.socketx.undertow.*;
import com.obsidiandynamics.socketx.util.*;

public final class EchoTest extends BaseClientServerTest {
  private static final int CYCLES = 2;
  private static final int CONNECTIONS = 5;
  private static final int MESSAGES = 10;
  private static final int PROGRESS_INTERVAL = 10;
  private static final int MAX_PORT_USE_COUNT = 10_000;

  @Test
  public void testJtJt() throws Exception {
    test(CYCLES, CONNECTIONS, MESSAGES, JettyServer.factory(), JettyClient.factory());
  }

  @Test
  public void testUtUt() throws Exception {
    test(CYCLES, CONNECTIONS, MESSAGES, UndertowServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testNtUt() throws Exception {
    test(CYCLES, CONNECTIONS, MESSAGES, NettyServer.factory(), UndertowClient.factory());
  }

  private void test(int cycles, int connections, int messages,
                    XServerFactory<? extends XEndpoint> serverFactory,
                    XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    for (int cycle = 0; cycle < cycles; cycle++) {
      if (cycle != 0) init();
      test(connections, messages, serverFactory, clientFactory);
      dispose();
      if (PROGRESS_INTERVAL != 0 && cycle % PROGRESS_INTERVAL == PROGRESS_INTERVAL - 1) {
        LOG_STREAM.format("cycle %,d\n", cycle);
      }
    }
  }

  private void test(int connections, int messages,
                    XServerFactory<? extends XEndpoint> serverFactory,
                    XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    final XServerConfig serverConfig = getDefaultServerConfig(false)
        .withScanInterval(1);
    final XEndpointListener<XEndpoint> serverListener = new XEndpointLambdaListener<>()
        .onText((endpoint, message) -> endpoint.send(message))
        .onBinary((endpoint, message) -> endpoint.send(message));
    createServer(serverFactory, serverConfig, serverListener);

    final XClientConfig clientConfig = getDefaultClientConfig()
        .withScanInterval(1);
    createClient(clientFactory, clientConfig);
    final XEndpointListener<XEndpoint> clientListener = createMockListener();
    final List<XEndpoint> endpoints = new ArrayList<>(connections);
    
    // connect all endpoints
    for (int i = 0; i < connections; i++) {
      endpoints.add(openClientEndpoint(false, serverConfig.port, clientListener));
    }
    
    // send one text and one binary frame over each client connection
    for (XEndpoint clientEndpoint : endpoints) {
      for (int i = 0; i < messages; i++) {
        clientEndpoint.send("test");
        clientEndpoint.send(toBuffer("test"));
      }
      clientEndpoint.flush();
    }
    
    for (XEndpoint serverEndpoint : server.getEndpointManager().getEndpoints()) {
      serverEndpoint.flush();
    }
    
    // assert receival of echo on clients
    final int expected = connections * messages;
    SocketUtils.await().until(() -> {
      verify(clientListener, times(expected)).onText(notNull(), eq("test"));
      verify(clientListener, times(expected)).onBinary(notNull(), eq(toBuffer("test")));
    });

    SocketUtils.drainPort(serverConfig.port, MAX_PORT_USE_COUNT);
  }
  
  private static ByteBuffer toBuffer(String str) {
    return ByteBuffer.wrap(str.getBytes());
  }
}