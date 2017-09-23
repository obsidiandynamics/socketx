package com.obsidiandynamics.socketx;

import java.util.*;

import com.obsidiandynamics.yconf.*;

import com.obsidiandynamics.socketx.ssl.*;

@Y
public abstract class XEndpointConfig<C extends XEndpointConfig<C>> {
  @YInject
  public long highWaterMark = Long.MAX_VALUE;
  
  @YInject
  public SSLContextProvider sslContextProvider;
  
  @YInject
  public Map<String, Object> attributes = Collections.emptyMap();
  
  public C withHighWaterMark(long highWaterMark) {
    this.highWaterMark = highWaterMark;
    return self();
  }
  
  public C withSSLContextProvider(SSLContextProvider sslContextProvider) {
    this.sslContextProvider = sslContextProvider;
    return self();
  }
  
  public C withAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
    return self();
  }
  
  @SuppressWarnings("unchecked")
  private C self() {
    return (C) this;
  }
}
