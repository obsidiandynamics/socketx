package com.obsidiandynamics.socketx.util;

import java.net.*;

/**
 *  A fluent builder incrementally assembling {@link URI} objects.
 */
public abstract class URIBuilder<U> {
  /**
   *  A pair of ports - one for HTTP, the other for HTTPS.
   */
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
  
  /**
   *  A way of supplying the port number.
   */
  @FunctionalInterface
  public interface PortProvider {
    int getPort(boolean https);
  }
  
  private PortProvider portProvider;
  
  private boolean https;
  
  private boolean webSocket;
  
  private String path = "/";
  
  private String host = "localhost";
  
  /**
   *  Assigns a constant port.
   *  
   *  @param port The port to set.
   *  @return The current instance of {@link URIBuilder} for chaining.
   */
  public final U withPort(int port) {
    return withPortProvider(https -> port);
  }
  
  /**
   *  Assigns a {@link PortProvider} that will be interrogated when
   *  the {@link #build()} method is called.
   *  
   *  @param portProvider The port provider.
   *  @return The current instance of {@link URIBuilder} for chaining.
   */
  public final U withPortProvider(PortProvider portProvider) {
    this.portProvider = portProvider;
    return self();
  }

  /**
   *  Indicates that this URI is for HTTPS traffic. This is a general flag
   *  encompassing all variants of the {@code https://...} scheme, and will produce
   *  {@code wss://...} if the {@link #webSocket} flag is set.
   *  
   *  @param https Whether HTTPS/WSS is used.
   *  @return The current instance of {@link URIBuilder} for chaining.
   */
  public final U withHttps(boolean https) {
    this.https = https;
    return self();
  }
  
  /**
   *  Indicates that the WebSocket protocol is used.
   *  
   *  @param webSocket Whether WS/WSS is used.
   *  @return The current instance of {@link URIBuilder} for chaining.
   */
  public final U withWebSocket(boolean webSocket) {
    this.webSocket = webSocket;
    return self();
  }

  /**
   *  Assigns a path.
   *  
   *  @param path The path to set.
   *  @return The current instance of {@link URIBuilder} for chaining.
   */
  public final U withPath(String path) {
    this.path = path;
    return self();
  }

  /**
   *  Assigns a host.
   *  @param host The host to set.
   *  @return The current instance of {@link URIBuilder} for chaining.
   */
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
  
  /**
   *  Builds the URI.
   *  
   *  @return The resulting {@link URI}.
   *  @exception URIBuilderException If a construction of the {@link URI} resulted in a {@link URISyntaxException}.
   */
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
  
  /**
   *  The base builder, which can be subclassed to offer additional methods.
   */
  public static final class BaseURIBuilder extends URIBuilder<BaseURIBuilder> {
    BaseURIBuilder() {}
  }
  
  /**
   *  Creates a new {@link BaseURIBuilder} instance for chaining.
   *  
   *  @return A new {@link BaseURIBuilder} instance.
   */
  public static BaseURIBuilder create() {
    return new BaseURIBuilder();
  }
}
