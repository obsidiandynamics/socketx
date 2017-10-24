package com.obsidiandynamics.socketx.ssl;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;

import javax.net.ssl.*;

import com.obsidiandynamics.socketx.util.*;
import com.obsidiandynamics.yconf.*;

@Y
public class JKSTrustManagerProvider implements TrustManagerProvider {
  @YInject
  String location;
  
  @YInject
  String storePassword;
  
  public final JKSTrustManagerProvider withLocation(String location) {
    this.location = location;
    return this;
  }

  public final JKSTrustManagerProvider withStorePassword(String storePassword) {
    this.storePassword = storePassword;
    return this;
  }

  @Override
  public final TrustManager[] getTrustManagers() throws KeyStoreException, NoSuchAlgorithmException, 
      CertificateException, FileNotFoundException, IOException, URISyntaxException, UnrecoverableKeyException {
    final KeyStore trustStore = JKS.loadKeyStore(ResourceLocator.asStream(new URI(location)), storePassword);
    return JKS.getTrustManagers(trustStore);
  }

  @Override
  public final String toString() {
    return "JKSTrustManagerProvider [location: " + location + "]";
  }
}
