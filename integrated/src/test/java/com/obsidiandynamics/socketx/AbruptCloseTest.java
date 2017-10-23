package com.obsidiandynamics.socketx;

import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.mockito.*;

import com.obsidiandynamics.junit.*;
import com.obsidiandynamics.socketx.jetty.*;
import com.obsidiandynamics.socketx.netty.*;
import com.obsidiandynamics.socketx.undertow.*;
import com.obsidiandynamics.socketx.util.*;

@RunWith(Parameterized.class)
public final class AbruptCloseTest extends BaseClientServerTest {
  @Parameterized.Parameters
  public static List<Object[]> data() {
    return TestCycle.once();
  }
  
  @Test
  public void testJtJtClientClose() throws Exception {
    testClientClose(JettyServer.factory(), JettyClient.factory());
  }
  
  @Test
  public void testUtUtClientClose() throws Exception {
    testClientClose(UndertowServer.factory(), UndertowClient.factory());
  }
  
  @Test
  public void testNtUtClientClose() throws Exception {
    testClientClose(NettyServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testJtJtServerClose() throws Exception {
    testServerClose(JettyServer.factory(), JettyClient.factory());
  }
  
  @Test
  public void testUtUtServerClose() throws Exception {
    testServerClose(UndertowServer.factory(), UndertowClient.factory());
  }
  
  @Test
  public void testNtUtServerClose() throws Exception {
    testServerClose(NettyServer.factory(), UndertowClient.factory());
  }

  private void testClientClose(XServerFactory<? extends XEndpoint> serverFactory,
                               XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    final XServerConfig serverConfig = getDefaultServerConfig(false)
        .withScanInterval(1);
    final XEndpointListener<XEndpoint> serverListener = createMockListener();
    createServer(serverFactory, serverConfig, serverListener);
    
    final XClientConfig clientConfig = getDefaultClientConfig()
        .withScanInterval(1);
    createClient(clientFactory, clientConfig);

    final XEndpointListener<XEndpoint> clientListener = createMockListener();
    final XEndpoint endpoint = openClientEndpoint(false, serverConfig.port, clientListener);
    SocketUtils.await().until(() -> {
      Mockito.verify(serverListener).onConnect(Mockito.notNull());
      Mockito.verify(clientListener).onConnect(Mockito.notNull());
    });
    
    endpoint.terminate();
    SocketUtils.await().until(() -> {
      Mockito.verify(serverListener).onClose(Mockito.notNull());
      Mockito.verify(clientListener).onClose(Mockito.notNull());
    });
  }

  private void testServerClose(XServerFactory<? extends XEndpoint> serverFactory,
                               XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    final XServerConfig serverConfig = getDefaultServerConfig(false);
    serverConfig.scanIntervalMillis = 1;
    final XEndpointListener<XEndpoint> serverListener = createMockListener();
    createServer(serverFactory, serverConfig, serverListener);
    
    final XClientConfig clientConfig = getDefaultClientConfig();
    clientConfig.scanIntervalMillis = 1;
    createClient(clientFactory, clientConfig);

    final XEndpointListener<XEndpoint> clientListener = createMockListener();
    openClientEndpoint(false, serverConfig.port, clientListener);
    
    SocketUtils.await().untilTrue(this::hasServerEndpoint);
    
    final XEndpoint endpoint = getServerEndpoint();
    endpoint.terminate();
    SocketUtils.await().until(() -> {
      Mockito.verify(serverListener).onClose(Mockito.notNull());
      Mockito.verify(clientListener).onClose(Mockito.notNull());
    });
  }
}
