package com.obsidiandynamics.socketx.util;

import java.net.*;

public abstract class URIBuilder<U> {
  public static final class Ports implements PortProvider {
    private final int port;
    private final int httpsPort;
    
    public Ports(int port, int httpsPort) {
      this.port = port;
      this.httpsPort = httpsPort;
    }

    @Override
    public int getPort(boolean https) {
      return https ? httpsPort : port;
    }

    @Override
    public String toString() {
      return "Ports [port=" + port + ", httpsPort=" + httpsPort + "]";
    }
  }
  
  @FunctionalInterface
  public interface PortProvider {
    int getPort(boolean https);
  }
  
  private PortProvider portProvider;
  
  private boolean https;
  
  private boolean webSocket;
  
  private String path = "/";
  
  private String host = "localhost";
  
  public final U withPort(int port) {
    return withPortProvider(https -> port);
  }
  
  public final U withPortProvider(PortProvider portProvider) {
    this.portProvider = portProvider;
    return self();
  }

  public final U withHttps(boolean https) {
    this.https = https;
    return self();
  }
  
  public final U withWebSocket(boolean webSocket) {
    this.webSocket = webSocket;
    return self();
  }

  public final U withPath(String path) {
    this.path = path;
    return self();
  }

  public final U withHost(String host) {
    this.host = host;
    return self();
  }
  
  @SuppressWarnings("unchecked")
  protected final U self() {
    return (U) this;
  }
  
  static final class URIBuilderException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    URIBuilderException(String m, Exception cause) { super(m, cause); }
  }
  
  public final URI build() {
    final StringBuilder sb = new StringBuilder()
        .append(getScheme())
        .append("://")
        .append(host)
        .append(':')
        .append(portProvider.getPort(https))
        .append(path);
    try {
      return new URI(sb.toString());
    } catch (URISyntaxException e) {
      throw new URIBuilderException("Error building URI", e);
    }
  }
  
  private String getScheme() {
    return webSocket ? (https ? "wss" : "ws") : (https ? "https" : "http");
  }
  
  public static final class BaseURIBuilder extends URIBuilder<BaseURIBuilder> {
    BaseURIBuilder() {}
  }
  
  public static BaseURIBuilder create() {
    return new BaseURIBuilder();
  }
}
