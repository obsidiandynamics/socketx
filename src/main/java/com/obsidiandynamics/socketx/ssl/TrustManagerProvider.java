package com.obsidiandynamics.socketx.ssl;

import javax.net.ssl.*;

public interface TrustManagerProvider {
  TrustManager[] getTrustManagers() throws Exception;
}
