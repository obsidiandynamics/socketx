package com.obsidiandynamics.socketx;

import java.net.*;

import org.junit.*;
import org.mockito.*;
import org.slf4j.*;

import com.obsidiandynamics.indigo.util.*;
import com.obsidiandynamics.socketx.util.*;

public abstract class BaseClientServerTest implements TestSupport {
  protected XServer<? extends XEndpoint> server;

  protected XClient<? extends XEndpoint> client;
  
  @Before
  public final void before() throws Exception {
    init();
  }

  @After
  public final void after() throws Exception {
    dispose();
  }
  
  protected void init() throws Exception {}
  
  protected void dispose() throws Exception {
    if (client != null) client.close();
    if (server != null) server.close();

    client = null;
    server = null;
  }

  protected static XServerConfig getDefaultServerConfig(boolean enableHttps) {
    return new XServerConfig() {{
      port = SocketUtils.getAvailablePort(8090);
      httpsPort = enableHttps ? SocketUtils.getAvailablePort(8543) : 0;
    }};
  }

  protected static XClientConfig getDefaultClientConfig() {
    return new XClientConfig();
  }
  
  @SuppressWarnings("unchecked")
  protected final void createServer(XServerFactory<? extends XEndpoint> serverFactory,
                                    XServerConfig config, XEndpointListener<XEndpoint> serverListener) throws Exception {
    server = serverFactory.create(config, InterceptingProxy.of(XEndpointListener.class, 
                                                               serverListener,
                                                               new LoggingInterceptor<>("s: ")));
  }
  
  protected final void createClient(XClientFactory<? extends XEndpoint> clientFactory, XClientConfig config) throws Exception {
    client = clientFactory.create(config);
  }
  
  @SuppressWarnings("unchecked")
  protected final XEndpoint openClientEndpoint(boolean https, int port, XEndpointListener<XEndpoint> clientListener) throws URISyntaxException, Exception {
    return client.connect(new URI(String.format("%s://localhost:%d/", https ? "wss" : "ws", port)),
                          InterceptingProxy.of(XEndpointListener.class, 
                                               clientListener,
                                               new LoggingInterceptor<>("c: ")));
  }
  
  protected final boolean hasServerEndpoint() {
    return ! server.getEndpointManager().getEndpoints().isEmpty();
  }
  
  protected final XEndpoint getServerEndpoint() {
    return server.getEndpointManager().getEndpoints().iterator().next();
  }
  
  @SuppressWarnings({ "unchecked" })
  protected static XEndpointListener<XEndpoint> createMockListener() {
    return Mockito.mock(XEndpointListener.class);
  }
  
  static final class Slf4jMockListener {
    final XEndpointListener<XEndpoint> mock;
    final XEndpointListener<XEndpoint> loggingListener;
   
    private Slf4jMockListener(XEndpointListener<XEndpoint> mock, XEndpointListener<XEndpoint> loggingListener) {
      this.mock = mock;
      this.loggingListener = loggingListener;
    }
  }
  
  @SuppressWarnings({ "unchecked" })
  protected static Slf4jMockListener createSlf4jMockListener(Logger logger, String prefix) {
    final XEndpointListener<XEndpoint> mock = Mockito.mock(XEndpointListener.class);
    final XEndpointListener<XEndpoint> loggingListener = 
        InterceptingProxy.of(XEndpointListener.class, mock, new Slf4jLoggingInterceptor<>(logger, prefix));
    return new Slf4jMockListener(mock, loggingListener);
  }
}
