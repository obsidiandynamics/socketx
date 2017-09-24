package com.obsidiandynamics.socketx;

import static junit.framework.TestCase.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;
import com.obsidiandynamics.socketx.jetty.*;
import com.obsidiandynamics.socketx.netty.*;
import com.obsidiandynamics.socketx.undertow.*;
import com.obsidiandynamics.socketx.util.*;

public final class HighWaterMarkTest extends BaseClientServerTest {
  @Test
  public void testJtJt() throws Exception {
    test(JettyServer.factory(), JettyClient.factory());
  }

  @Test
  public void testUtUt() throws Exception {
    test(UndertowServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testNtUt() throws Exception {
    test(NettyServer.factory(), UndertowClient.factory());
  }

  private void test(XServerFactory<? extends XEndpoint> serverFactory,
                    XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    final int highWaterMark = 1;
    final XServerConfig serverConfig = getDefaultServerConfig(false)
        .withScanInterval(1)
        .withHighWaterMark(highWaterMark);
    final XEndpointListener<XEndpoint> serverListener = createMockListener();
    createServer(serverFactory, serverConfig, serverListener);

    final XClientConfig clientConfig = getDefaultClientConfig()
        .withScanInterval(1);
    createClient(clientFactory, clientConfig);
    
    final CyclicBarrier receiveStarted = new CyclicBarrier(2);
    final CyclicBarrier receiveComplete = new CyclicBarrier(2);
    final AtomicBoolean complete = new AtomicBoolean();
    final XEndpointListener<XEndpoint> clientListener = new XEndpointLambdaListener<>()
        .onText((endpoint, message) -> {
          if (! complete.get()) {
            TestSupport.await(receiveStarted);
            TestSupport.await(receiveComplete);
          }
        });
    openClientEndpoint(false, serverConfig.port, clientListener);
    
    SocketUtils.await().untilTrue(() -> ! server.getEndpointManager().getEndpoints().isEmpty());

    final int messages = highWaterMark * 100;
    for (XEndpoint endpoint : server.getEndpointManager().getEndpoints()) {
      for (int i = 0; i < messages; i++) {
        endpoint.send("test", null);
        final long backlog = endpoint.getBacklog();
        if (backlog > highWaterMark) {
          TestSupport.await(receiveStarted);
          complete.set(true);
          TestSupport.await(receiveComplete);
          throw new AssertionError("backlog=" + backlog);
        }
      }
      TestSupport.await(receiveStarted);
      try {
        assertTrue("backlog=" + endpoint.getBacklog(), endpoint.getBacklog() <= highWaterMark);
      } finally { 
        complete.set(true);
        TestSupport.await(receiveComplete);
      }
    }
  }
}