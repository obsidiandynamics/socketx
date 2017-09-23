package com.obsidiandynamics.socketx.ssl;

import java.security.cert.*;

import javax.net.ssl.*;

import com.obsidiandynamics.yconf.*;

@Y
public final class LenientX509TrustManagerProvider implements TrustManagerProvider {
  @Override
  public TrustManager[] getTrustManagers() throws Exception {
    return new TrustManager[] {
      new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
        
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
        
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
      }
    };
  }
  
  @Override
  public String toString() {
    return LenientX509TrustManagerProvider.class.getSimpleName();
  }
}
