package com.obsidiandynamics.socketx.ssl;

import javax.net.ssl.*;

@FunctionalInterface
public interface SSLContextProvider {
  SSLContext getSSLContext() throws Exception;
}
