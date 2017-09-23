package com.obsidiandynamics.socketx.ssl;

import javax.net.ssl.*;

import com.obsidiandynamics.yconf.*;

@Y
public final class NullKeyManagerProvider implements KeyManagerProvider {
  @Override
  public KeyManager[] getKeyManagers() {
    return null;
  }

  @Override
  public String toString() {
    return NullKeyManagerProvider.class.getSimpleName();
  }
}
