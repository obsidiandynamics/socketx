package com.obsidiandynamics.socketx.ssl;

import static org.junit.Assert.*;

import javax.net.ssl.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

public final class SSLContextProvidersTest {
  private static void assertBasics(SSLContextProvider provider) throws Exception {
    assertNotNull(provider.getSSLContext());
    TestSupport.assertToString(provider);
  }
  
  @Test
  public void testDefault() throws Exception {
    assertBasics(new DefaultSSLContextProvider());
  }

  @Test
  public void testComposite() throws Exception {
    class TestKeyManagerProvider implements KeyManagerProvider {
      @Override public KeyManager[] getKeyManagers() throws Exception {
        return null;
      }
    }
    
    class TestTrustManagerProvider implements TrustManagerProvider {
      @Override public TrustManager[] getTrustManagers() throws Exception {
        return null;
      }
    }
    
    final CompositeSSLContextProvider provider = new CompositeSSLContextProvider();
    assertEquals(TestKeyManagerProvider.class, 
                 provider.withKeyManagerProvider(new TestKeyManagerProvider()).keyManagerProvider.getClass());
    assertEquals(TestTrustManagerProvider.class, 
                 provider.withTrustManagerProvider(new TestTrustManagerProvider()).trustManagerProvider.getClass());
    assertBasics(provider);
    assertNotNull(CompositeSSLContextProvider.getDevClientDefault());
    assertNotNull(CompositeSSLContextProvider.getDevServerDefault());
  }
  
  @Test
  public void testNullKey() {
    final NullKeyManagerProvider keyProvider = new NullKeyManagerProvider();
    assertNull(keyProvider.getKeyManagers());
    TestSupport.assertToString(keyProvider);
  }
  
  @Test
  public void testNullTrust() {
    final NullTrustManagerProvider trustProvider = new NullTrustManagerProvider();
    assertNull(trustProvider.getTrustManagers());
    TestSupport.assertToString(trustProvider);
  }
  
  @Test
  public void testLenientTrust() throws Exception {
    final LenientX509TrustManagerProvider trustProvider = new LenientX509TrustManagerProvider();
    final TrustManager[] trustManagers = trustProvider.getTrustManagers();
    assertNotNull(trustManagers);
    assertEquals(1, trustManagers.length);
    final X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
    assertEquals(0, trustManager.getAcceptedIssuers().length);
    trustManager.checkServerTrusted(null, null);
    trustManager.checkClientTrusted(null, null);
    TestSupport.assertToString(trustProvider);
  }
  
  @Test
  public void testJKSKey() throws Exception {
    final JKSKeyManagerProvider keyProvider = new JKSKeyManagerProvider()
        .withLocation("cp://keystore.jks")
        .withStorePassword("storepass")
        .withKeyPassword("keypass");
    assertNotNull(keyProvider.getKeyManagers());
    TestSupport.assertToString(keyProvider);
  }
  
  @Test
  public void testJKSTrust() throws Exception {
    final JKSTrustManagerProvider trustProvider = new JKSTrustManagerProvider()
        .withLocation("cp://keystore.jks")
        .withStorePassword("storepass");
    assertNotNull(trustProvider.getTrustManagers());
    TestSupport.assertToString(trustProvider);
  }
}
