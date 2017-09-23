package com.obsidiandynamics.socketx.ssl;

import javax.net.ssl.*;

import com.obsidiandynamics.yconf.*;

@Y
public class CompositeSSLContextProvider implements SSLContextProvider {
  @YInject
  KeyManagerProvider keyManagerProvider = new NullKeyManagerProvider();

  @YInject
  TrustManagerProvider trustManagerProvider = new NullTrustManagerProvider();

  public final CompositeSSLContextProvider withKeyManagerProvider(KeyManagerProvider keyManagerProvider) {
    this.keyManagerProvider = keyManagerProvider;
    return this;
  }

  public final CompositeSSLContextProvider withTrustManagerProvider(TrustManagerProvider trustManagerProvider) {
    this.trustManagerProvider = trustManagerProvider;
    return this;
  }

  @Override
  public final SSLContext getSSLContext() throws Exception {
    final SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(keyManagerProvider.getKeyManagers(), 
                    trustManagerProvider.getTrustManagers(),
                    null);
    return sslContext;
  }

  @Override
  public final String toString() {
    return "CompositeSSLContextProvider [keyManagerProvider: " + keyManagerProvider + ", trustManagerProvider: "
        + trustManagerProvider + "]";
  }

  public static CompositeSSLContextProvider getDevServerDefault() {
    return new CompositeSSLContextProvider()
        .withKeyManagerProvider(new JKSKeyManagerProvider()
                                .withLocation("cp://keystore.jks")
                                .withStorePassword("storepass")
                                .withKeyPassword("keypass"))
        .withTrustManagerProvider(new JKSTrustManagerProvider()
                                  .withLocation("cp://keystore.jks")
                                  .withStorePassword("storepass"));
  }
  
  public static CompositeSSLContextProvider getDevClientDefault() {
    return new CompositeSSLContextProvider()
        .withTrustManagerProvider(new LenientX509TrustManagerProvider());
  }
}
