package com.obsidiandynamics.socketx.jetty;

import java.util.*;

import org.eclipse.jetty.websocket.server.*;
import org.eclipse.jetty.websocket.servlet.*;

import com.obsidiandynamics.socketx.*;

final class JettyEndpointManager extends WebSocketHandler implements XEndpointManager<JettyEndpoint> {
  private final int idleTimeoutMillis;
  
  private final XEndpointConfig<?> config;
  
  private final XEndpointListener<? super JettyEndpoint> listener;
  
  private final XEndpointScanner<JettyEndpoint> scanner;

  JettyEndpointManager(XEndpointScanner<JettyEndpoint> scanner, int idleTimeoutMillis, 
                       XEndpointConfig<?> config, XEndpointListener<? super JettyEndpoint> listener) {
    this.idleTimeoutMillis = idleTimeoutMillis;
    this.config = config;
    this.listener = listener;
    this.scanner = scanner;
  }
  
  @Override
  public void configure(WebSocketServletFactory factory) {
    if (idleTimeoutMillis != 0) {
      factory.getPolicy().setIdleTimeout(idleTimeoutMillis);
    }
    
    factory.setCreator(new WebSocketCreator() {
      @Override public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
        return createEndpoint();
      }
    });
  }
  
  JettyEndpoint createEndpoint() {
    final JettyEndpoint endpoint = new JettyEndpoint(JettyEndpointManager.this);
    return endpoint;
  }
  
  void add(JettyEndpoint endpoint) {
    scanner.addEndpoint(endpoint);
  }
  
  void remove(JettyEndpoint endpoint) {
    scanner.removeEndpoint(endpoint);
  }
  
  XEndpointListener<? super JettyEndpoint> getListener() {
    return listener;
  }
  
  XEndpointConfig<?> getConfig() {
    return config;
  }
  
  @Override
  public Collection<JettyEndpoint> getEndpoints() {
    return scanner.getEndpoints();
  }
}
