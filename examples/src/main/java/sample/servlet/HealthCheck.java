package sample.servlet;

import java.awt.*;
import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.obsidiandynamics.socketx.*;
import com.obsidiandynamics.socketx.ssl.*;
import com.obsidiandynamics.socketx.undertow.*;

public final class HealthCheck {
  public static class HealthCheckServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
      System.out.format("Initialised with %s\n", config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.getWriter().write("Cruizin'");
    }
  }
  
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
