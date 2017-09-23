package com.obsidiandynamics.socketx.ssl;

import java.security.*;

import javax.net.ssl.*;

import com.obsidiandynamics.yconf.*;

@Y
public final class DefaultSSLContextProvider implements SSLContextProvider {
  @Override
  public SSLContext getSSLContext() throws NoSuchAlgorithmException {
    return SSLContext.getDefault();
  }

  @Override
  public String toString() {
    return DefaultSSLContextProvider.class.getSimpleName();
  }
}
