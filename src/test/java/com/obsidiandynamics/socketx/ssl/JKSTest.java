package com.obsidiandynamics.socketx.ssl;

import static org.junit.Assert.*;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;

import javax.net.ssl.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;
import com.obsidiandynamics.socketx.util.*;

public final class JKSTest {
  @Test
  public void testConformance() throws Exception {
    TestSupport.assertUtilityClassWellDefined(JKS.class);
  }
  
  @Test
  public void testCreateSSLContext() throws Exception {
    final KeyStore keyStore = JKS
        .loadKeyStore(ResourceLocator.asStream(new URI("cp://keystore.jks")), "storepass");
    final SSLContext sslContext = JKS.createSSLContext(keyStore, "keypass", keyStore);
    assertNotNull(sslContext);
  }
  
  @Test(expected=IOException.class)
  public void testLoadKeyStoreWrongPassword() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, URISyntaxException {
    JKS.loadKeyStore(ResourceLocator.asStream(new URI("cp://keystore.jks")), "badpass");
  }
}
