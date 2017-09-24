package com.obsidiandynamics.socketx;

import static junit.framework.TestCase.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

public final class XClientConfigTest {
  @Test
  public void testToString() {
    TestSupport.assertToString(new XClientConfig());
  }

  @Test
  public void testIdleTimout() {
    final XClientConfig config = new XClientConfig().withIdleTimeout(0);
    assertFalse(config.hasIdleTimeout());
    config.withIdleTimeout(1000);
    assertEquals(1000, config.idleTimeoutMillis);
    assertTrue(config.hasIdleTimeout());
  }

  @Test
  public void testScanInterval() {
    assertEquals(2000, new XClientConfig().withScanInterval(2000).scanIntervalMillis);
  }
}
