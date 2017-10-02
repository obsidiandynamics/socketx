package com.obsidiandynamics.socketx;

import static junit.framework.TestCase.*;

import java.util.*;

import javax.net.ssl.*;

import org.junit.*;

import com.obsidiandynamics.socketx.ssl.*;

public final class XEndpointConfigTest {
  @Test
  public void testIdleTimout() {
    final DerivedEndpointConfig config = new DerivedEndpointConfig().withIdleTimeout(0);
    assertFalse(config.hasIdleTimeout());
    config.withIdleTimeout(1000);
    assertEquals(1000, config.idleTimeoutMillis);
    assertTrue(config.hasIdleTimeout());
  }

  @Test
  public void testScanInterval() {
    assertEquals(2000, new DerivedEndpointConfig().withScanInterval(2000).scanIntervalMillis);
  }
  
  @Test
  public void testHighWaterMark() {
    assertEquals(1000, new DerivedEndpointConfig().withHighWaterMark(1000).highWaterMark);
  }
  
  @Test
  public void testSSLContextProvider() {
    class TestSSLContextProvider implements SSLContextProvider {
      @Override public SSLContext getSSLContext() throws Exception {
        return null;
      }
    }
    assertEquals(TestSSLContextProvider.class, 
                 new DerivedEndpointConfig()
                 .withSSLContextProvider(new TestSSLContextProvider()).sslContextProvider.getClass());
  }

  @Test
  public void testAttributes() {
    final Map<String, Object> atts = Collections.singletonMap("foo", "bar");
    assertEquals(atts, new DerivedEndpointConfig().withAttributes(atts).attributes);
  }
}
