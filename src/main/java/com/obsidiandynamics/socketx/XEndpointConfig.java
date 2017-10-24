package com.obsidiandynamics.socketx;

import java.util.*;

import com.obsidiandynamics.socketx.ssl.*;
import com.obsidiandynamics.yconf.*;

@Y
public abstract class XEndpointConfig<C extends XEndpointConfig<C>> {
  @YInject
  public int idleTimeoutMillis = 300_000;
  
  @YInject
  public int scanIntervalMillis = 1_000;
  
  @YInject
  public long highWaterMark = Long.MAX_VALUE;
  
  @YInject
  public SSLContextProvider sslContextProvider;
  
  @YInject
  public Map<String, Object> attributes = Collections.emptyMap();
  
  public final C withScanInterval(int scanIntervalMillis) {
    this.scanIntervalMillis = scanIntervalMillis;
    return self();
  }
  
  public final boolean hasIdleTimeout() {
    return idleTimeoutMillis != 0;
  }
  
  public final C withIdleTimeout(int idleTimeoutMillis) {
    this.idleTimeoutMillis = idleTimeoutMillis;
    return self();
  }
  
  public final C withHighWaterMark(long highWaterMark) {
    this.highWaterMark = highWaterMark;
    return self();
  }
  
  public final C withSSLContextProvider(SSLContextProvider sslContextProvider) {
    this.sslContextProvider = sslContextProvider;
    return self();
  }
  
  public final C withAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
    return self();
  }
  
  @SuppressWarnings("unchecked")
  private C self() {
    return (C) this;
  }
}
