package com.obsidiandynamics.socketx.ssl;

import javax.net.ssl.*;

public interface KeyManagerProvider {
  KeyManager[] getKeyManagers() throws Exception;
}
