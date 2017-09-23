package com.obsidiandynamics.socketx.ssl;

import javax.net.ssl.*;

import com.obsidiandynamics.yconf.*;

@Y
public final class NullTrustManagerProvider implements TrustManagerProvider {
  @Override
  public TrustManager[] getTrustManagers() {
    return null;
  }

  @Override
  public String toString() {
    return NullTrustManagerProvider.class.getSimpleName();
  }
}
