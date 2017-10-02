package sample.servlet;

import java.awt.*;
import java.net.*;

import com.obsidiandynamics.socketx.*;
import com.obsidiandynamics.socketx.ssl.*;
import com.obsidiandynamics.socketx.undertow.*;

public final class HealthCheck {
  public static void main(String[] args) throws Exception {
    final XServer<?> server = UndertowServer
        .factory()
        .create(new XServerConfig()
                .withPath("/echo")
                .withPort(8080)
                .withHttpsPort(8443)
                .withSSLContextProvider(CompositeSSLContextProvider.getDevServerDefault())
                .withServlets(new XMappedServlet("/health/*", HealthCheckServlet.class)),
                new XEndpointLambdaListener<>());

    if (Desktop.isDesktopSupported()) {
      Desktop.getDesktop().browse(new URI("http://localhost:8080/health"));
    }
    
    Thread.sleep(60_000);
    server.close();
  }
}
