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
    assertEquals(1000, new XClientConfig().withIdleTimeout(1000).idleTimeoutMillis);
  }

  @Test
  public void testScanInterval() {
    assertEquals(2000, new XClientConfig().withScanInterval(2000).scanIntervalMillis);
  }
}
