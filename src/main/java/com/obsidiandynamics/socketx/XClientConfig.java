package com.obsidiandynamics.socketx;

import com.obsidiandynamics.yconf.*;

import com.obsidiandynamics.socketx.ssl.*;

@Y
public class XClientConfig extends XEndpointConfig<XClientConfig> {
  @YInject
  public int idleTimeoutMillis = 300_000;
  
  @YInject
  public int scanIntervalMillis = 1_000;
  
  {
    sslContextProvider = new DefaultSSLContextProvider();
  }
  
  public boolean hasIdleTimeout() {
    return idleTimeoutMillis != 0;
  }
  
  public XClientConfig withIdleTimeout(int idleTimeoutMillis) {
    this.idleTimeoutMillis = idleTimeoutMillis;
    return this;
  }
  
  public XClientConfig withScanInterval(int scanIntervalMillis) {
    this.scanIntervalMillis = scanIntervalMillis;
    return this;
  }

  @Override
  public String toString() {
    return "XClientConfig [idleTimeoutMillis: " + idleTimeoutMillis + ", scanIntervalMillis: " + scanIntervalMillis
           + ", highWaterMark: " + highWaterMark + ", sslContextProvider: " + sslContextProvider
           + ", attributes: " + attributes + "]";
  }
}
