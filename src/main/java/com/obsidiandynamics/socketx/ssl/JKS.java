package com.obsidiandynamics.socketx.ssl;

import java.io.*;
import java.security.*;
import java.security.cert.*;

import javax.net.ssl.*;

public final class JKS {
  private JKS() {}

  public static SSLContext createSSLContext(KeyStore keyStore, String keyPassword, KeyStore trustStore) throws Exception {
    final SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(getKeyManagers(keyStore, keyPassword), getTrustManagers(trustStore), null);
    return sslContext;
  }
  
  public static KeyManager[] getKeyManagers(KeyStore keyStore, String keyPassword) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
    final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(keyStore, keyPassword.toCharArray());
    return keyManagerFactory.getKeyManagers();
  }
  
  public static TrustManager[] getTrustManagers(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
    final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(trustStore);
    return trustManagerFactory.getTrustManagers();
  }

  public static KeyStore loadKeyStore(InputStream stream, String storePassword) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    try (InputStream is = stream) {
      final KeyStore loadedKeystore = KeyStore.getInstance("JKS");
      loadedKeystore.load(is, storePassword.toCharArray());
      return loadedKeystore;
    }
  }
}
