package com.obsidiandynamics.socketx;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class XClientConfigTest {
  @Test
  public void testToString() {
    Assertions.assertToStringOverride(new XClientConfig());
  }
}
