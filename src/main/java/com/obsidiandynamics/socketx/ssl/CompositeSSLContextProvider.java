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

  /**
   *  Provides server defaults suitable for a development environment based on a self-signed certificate.<p>
   *  
   *  The keystore is generated using the following command and placed in {@code src/main/resources}:<br/>
   *  {@code keytool -genkeypair -keyalg RSA -keysize 4096 -keystore keystore-dev.jks -keypass keypass -storepass storepass -validity 99999}
   *  
   *  @return An SSL context provider for a dev server.
   */
  public static CompositeSSLContextProvider getDevServerDefault() {
    return new CompositeSSLContextProvider()
        .withKeyManagerProvider(new JKSKeyManagerProvider()
                                .withLocation("cp://keystore-dev.jks")
                                .withStorePassword("storepass")
                                .withKeyPassword("keypass"))
        .withTrustManagerProvider(new JKSTrustManagerProvider()
                                  .withLocation("cp://keystore-dev.jks")
                                  .withStorePassword("storepass"));
  }
  
  /**
   *  Provides client defaults suitable for a development environment based a lenient trust manager -
   *  accepting <em>any</em> certificate offered by the server.
   *  
   *  @return An SSL context provider for a dev client.
   */
  public static CompositeSSLContextProvider getDevClientDefault() {
    return new CompositeSSLContextProvider()
        .withTrustManagerProvider(new LenientX509TrustManagerProvider());
  }
}
