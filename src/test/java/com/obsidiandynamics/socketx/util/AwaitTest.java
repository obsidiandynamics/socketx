package com.obsidiandynamics.socketx.util;

import static com.obsidiandynamics.indigo.util.TestSupport.*;
import static junit.framework.TestCase.*;

import java.util.concurrent.*;

import org.junit.*;

public final class AwaitTest {
  @Test(expected=InterruptedException.class)
  public void testInterrupt() throws InterruptedException, TimeoutException {
    Thread.currentThread().interrupt();
    Await.perpetual(20, () -> false);
  }
  
  @Test
  public void testBoundedConditionTimedOut() throws InterruptedException {
    final long start = System.currentTimeMillis();
    final boolean r = Await.bounded(20, () -> false);
    assertFalse(r);
    final long elapsed = System.currentTimeMillis() - start;
    assertTrue("Elapsed " + elapsed, elapsed >= 20);
  }
  
  @Test
  public void testBoundedConditionPassed() throws InterruptedException {
    final boolean r = Await.bounded(20, () -> true);
    assertTrue(r);
  }
  
  @Test
  public void testBoundedZeroMillisConditionTimedOut() throws InterruptedException {
    final long start = System.currentTimeMillis();
    final boolean r = Await.bounded(20, 0, () -> false);
    assertFalse(r);
    final long elapsed = System.currentTimeMillis() - start;
    assertTrue("Elapsed " + elapsed, elapsed >= 0);
  }

  @Test
  public void testPerpetualConditionPassed() throws InterruptedException {
    Await.perpetual(() -> true);
    Await.perpetual(10, () -> true);
  }
  
  @Test(expected=TimeoutException.class)
  public void testBoundedTimeoutException() throws InterruptedException, TimeoutException {
    Await.boundedTimeout(20, () -> false);
  }
  
  @Test()
  public void testBoundedTimeoutNoException() throws InterruptedException, TimeoutException {
    Await.boundedTimeout(20, () -> true);
  }

  @Test
  public void testConformance() throws Exception {
    assertUtilityClassWellDefined(Await.class);
  }
}
