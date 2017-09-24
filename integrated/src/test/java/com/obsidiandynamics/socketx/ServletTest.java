package com.obsidiandynamics.socketx;

import static junit.framework.TestCase.*;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.junit.*;

import com.obsidiandynamics.socketx.jetty.*;
import com.obsidiandynamics.socketx.netty.*;
import com.obsidiandynamics.socketx.undertow.*;
import com.obsidiandynamics.socketx.util.*;

public final class ServletTest extends BaseClientServerTest {
  private static final int CYCLES = 2;
  private static final int PROGRESS_INTERVAL = 10;
  private static final int MAX_PORT_USE_COUNT = 10_000;
  
  private CloseableHttpClient httpClient;
  
  @Override
  protected void init() throws Exception {
    super.init();
    httpClient = HttpClientBuilder.create().build();
  }
  
  @Override
  protected void dispose() throws Exception {
    super.dispose();
    if (httpClient != null) httpClient.close();
    httpClient = null;
  }

  @Test
  public void testJt() throws Exception {
    test(CYCLES, JettyServer.factory(), JettyClient.factory());
  }

  @Test
  public void testUt() throws Exception {
    test(CYCLES, UndertowServer.factory(), UndertowClient.factory());
  }

  @Test(expected=UnsupportedOperationException.class)
  public void testNt() throws Exception {
    test(CYCLES, NettyServer.factory(), UndertowClient.factory());
  }

  private void test(int cycles,
                    XServerFactory<? extends XEndpoint> serverFactory,
                    XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    for (int cycle = 0; cycle < cycles; cycle++) {
      if (cycle != 0) init();
      try {
        test(serverFactory, clientFactory);
      } finally {
        dispose();
      }
      if (PROGRESS_INTERVAL != 0 && cycle % PROGRESS_INTERVAL == PROGRESS_INTERVAL - 1) {
        LOG_STREAM.format("cycle %,d\n", cycle);
      }
    }
  }
  
  private static final String SERVLET_PATH = "/test";
  private static final String SERVLET_RESPONSE = "All good";
  
  public static class TestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.getWriter().write(SERVLET_RESPONSE);
    }
  }

  private void test(XServerFactory<? extends XEndpoint> serverFactory,
                    XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    final XServerConfig serverConfig = getDefaultServerConfig(false)
        .withPath("/websocket")
        .withServlets(new XMappedServlet(SERVLET_PATH + "/*", TestServlet.class));
    createServer(serverFactory, serverConfig, new XEndpointLambdaListener<>());

    final String url = String.format("http://localhost:%d%s", serverConfig.port, SERVLET_PATH);
    log("url=%s\n", url);
    final HttpGet get = new HttpGet(url);
    final HttpResponse response = httpClient.execute(get);
    assertEquals(200, response.getStatusLine().getStatusCode());
    
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    response.getEntity().writeTo(baos);
    final byte[] responseBytes = baos.toByteArray();
    final String responseStr = new String(responseBytes);
    assertEquals(SERVLET_RESPONSE, responseStr);
    
    SocketUtils.drainPort(serverConfig.port, MAX_PORT_USE_COUNT);
  }
}