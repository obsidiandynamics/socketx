package com.obsidiandynamics.socketx.ssl;

import javax.net.ssl.*;

public interface SSLContextProvider {
  SSLContext getSSLContext() throws Exception;
}
