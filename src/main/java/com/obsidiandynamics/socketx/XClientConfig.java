package com.obsidiandynamics.socketx;

import com.obsidiandynamics.socketx.ssl.*;
import com.obsidiandynamics.yconf.*;

@Y
public class XClientConfig extends XEndpointConfig<XClientConfig> {
  {
    sslContextProvider = CompositeSSLContextProvider.getDevServerDefault();
  }
  
  @Override
  public String toString() {
    return "XClientConfig [idleTimeoutMillis: " + idleTimeoutMillis + ", scanIntervalMillis: " + scanIntervalMillis
           + ", highWaterMark: " + highWaterMark + ", sslContextProvider: " + sslContextProvider
           + ", attributes: " + attributes + "]";
  }
}
