package com.obsidiandynamics.socketx;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;

import java.nio.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.junit.*;
import org.mockito.*;

import com.obsidiandynamics.indigo.util.*;
import com.obsidiandynamics.socketx.jetty.*;
import com.obsidiandynamics.socketx.netty.*;
import com.obsidiandynamics.socketx.undertow.*;
import com.obsidiandynamics.socketx.util.*;

public final class HighWaterMarkTest extends BaseClientServerTest {
  private static final boolean TEXT = true;
  private static final boolean BINARY = false;
  
  @Test
  public void testJtJtText() throws Exception {
    test(JettyServer.factory(), JettyClient.factory(), TEXT);
  }
  
  @Test
  public void testJtJtBinary() throws Exception {
    test(JettyServer.factory(), JettyClient.factory(), BINARY);
  }

  @Test
  public void testUtUtText() throws Exception {
    test(UndertowServer.factory(), UndertowClient.factory(), TEXT);
  }

  @Test
  public void testUtUtBinary() throws Exception {
    test(UndertowServer.factory(), UndertowClient.factory(), BINARY);
  }

  @Test
  public void testNtUtText() throws Exception {
    test(NettyServer.factory(), UndertowClient.factory(), TEXT);
  }

  @Test
  public void testNtUtBinary() throws Exception {
    test(NettyServer.factory(), UndertowClient.factory(), BINARY);
  }

  private void test(XServerFactory<? extends XEndpoint> serverFactory,
                    XClientFactory<? extends XEndpoint> clientFactory,
                    boolean text) throws Exception {
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
    final Runnable receivedHandler = () -> {
      if (! complete.get()) {
        TestSupport.await(receiveStarted);
        TestSupport.await(receiveComplete);
      }
    };
    final XEndpointListener<XEndpoint> clientListener = new XEndpointLambdaListener<>()
        .onText((endpoint, message) -> receivedHandler.run())
        .onBinary((endpoint, message) -> receivedHandler.run());
    openClientEndpoint(false, serverConfig.port, clientListener);
    
    SocketUtils.await().untilTrue(() -> ! server.getEndpointManager().getEndpoints().isEmpty());

    final int messages = highWaterMark * 100;
    final XSendCallback callback = Mockito.mock(XSendCallback.class);
    final int payloadLength = 8192;
    final String textPayload = BinaryUtils.randomHexString(payloadLength);
    final byte[] binaryPayload = BinaryUtils.randomBytes(payloadLength);
    for (XEndpoint endpoint : server.getEndpointManager().getEndpoints()) {
      for (int i = 0; i < messages; i++) {
        if (text) {
          endpoint.send(textPayload, i % 2 == 0 ? callback : null);
        } else {
          endpoint.send(ByteBuffer.wrap(binaryPayload), i % 2 == 0 ? callback : null);
        }
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
    
    verify(callback, atLeastOnce()).onSkip(any(XEndpoint.class));
  }
}