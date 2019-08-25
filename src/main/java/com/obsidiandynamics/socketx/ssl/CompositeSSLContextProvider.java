package com.obsidiandynamics.socketx.ssl;

import javax.net.ssl.*;

import com.obsidiandynamics.yconf.*;

@Y
public class CompositeSSLContextProvider implements SSLContextProvider {
  private static final String DEF_STORE_LOCATION = "cp://keystore-dev.jks";
  private static final String DEF_TRUSTSTORE_PASSWORD = "storepass";
  private static final String DEF_KEYSTORE_PASSWORD = "keypass";
  
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
    return CompositeSSLContextProvider.class.getSimpleName() + " [keyManagerProvider: " + keyManagerProvider + ", trustManagerProvider: "
        + trustManagerProvider + "]";
  }

  /**
   *  Provides server defaults suitable for a development environment based on a self-signed certificate.<p>
   *  
   *  The keystore is generated using the following command and placed in {@code src/main/resources}:<br>
   *  {@code keytool -genkeypair -keyalg RSA -keysize 4096 -keystore keystore-dev.jks -keypass keypass -storepass storepass -validity 99999}
   *  
   *  @return An SSL context provider for a dev server.
   */
  public static CompositeSSLContextProvider getDevServerDefault() {
    return new CompositeSSLContextProvider()
        .withKeyManagerProvider(new JKSKeyManagerProvider()
                                .withLocation(DEF_STORE_LOCATION)
                                .withStorePassword(DEF_TRUSTSTORE_PASSWORD)
                                .withKeyPassword(DEF_KEYSTORE_PASSWORD))
        .withTrustManagerProvider(new JKSTrustManagerProvider()
                                  .withLocation(DEF_STORE_LOCATION)
                                  .withStorePassword(DEF_TRUSTSTORE_PASSWORD));
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
