package com.obsidiandynamics.socketx.ssl;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;

import javax.net.ssl.*;

import com.obsidiandynamics.socketx.util.*;
import com.obsidiandynamics.yconf.*;

@Y
public class JKSKeyManagerProvider implements KeyManagerProvider {
  @YInject
  String location;
  
  @YInject
  String storePassword;
  
  @YInject
  String keyPassword;
  
  public final JKSKeyManagerProvider withLocation(String location) {
    this.location = location;
    return this;
  }

  public final JKSKeyManagerProvider withStorePassword(String storePassword) {
    this.storePassword = storePassword;
    return this;
  }

  public final JKSKeyManagerProvider withKeyPassword(String keyPassword) {
    this.keyPassword = keyPassword;
    return this;
  }

  @Override
  public final KeyManager[] getKeyManagers() throws KeyStoreException, NoSuchAlgorithmException, 
      CertificateException, FileNotFoundException, IOException, URISyntaxException, UnrecoverableKeyException {
    final KeyStore keyStore = JKS.loadKeyStore(ResourceLocator.asStream(new URI(location)), storePassword);
    return JKS.getKeyManagers(keyStore, keyPassword);
  }

  @Override
  public final String toString() {
    return "JKSKeyManagerProvider [location: " + location + "]";
  }
}
