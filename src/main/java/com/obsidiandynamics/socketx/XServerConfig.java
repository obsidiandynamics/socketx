package com.obsidiandynamics.socketx;

import java.util.*;

import com.obsidiandynamics.socketx.ssl.*;
import com.obsidiandynamics.yconf.*;

@Y
public class XServerConfig extends XEndpointConfig<XServerConfig> {
  @YInject
  public int port = 8080;
  
  @YInject
  public int httpsPort = 0;
  
  @YInject
  public String path = "/";
  
  @YInject
  public int pingIntervalMillis = 60_000;
  
  @YInject
  public XMappedServlet[] servlets = new XMappedServlet[0];
  
  {
    sslContextProvider = CompositeSSLContextProvider.getDevServerDefault();
  }
  
  public XServerConfig withPort(int port) {
    this.port = port;
    return this;
  }
  
  public XServerConfig withHttpsPort(int httpsPort) {
    this.httpsPort = httpsPort;
    return this;
  }
  
  public XServerConfig withPath(String path) {
    this.path = path;
    return this;
  }
  
  public XServerConfig withPingInterval(int pingIntervalMillis) {
    this.pingIntervalMillis = pingIntervalMillis;
    return this;
  }
  
  public XServerConfig withServlets(XMappedServlet... servlets) {
    this.servlets = servlets;
    return this;
  }

  @Override
  public String toString() {
    return "XServerConfig [port: " + port + ", httpsPort: " + httpsPort + ", path: " + path + ", idleTimeoutMillis: "
           + idleTimeoutMillis + ", pingIntervalMillis: " + pingIntervalMillis + ", scanIntervalMillis: "
           + scanIntervalMillis + ", servlets: " + Arrays.toString(servlets) + ", highWaterMark: " + highWaterMark
           + ", sslContextProvider: " + sslContextProvider + ", attributes: " + attributes + "]";
  }
}
