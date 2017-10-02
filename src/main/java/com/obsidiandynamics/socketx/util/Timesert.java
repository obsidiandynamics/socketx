package com.obsidiandynamics.socketx.util;

import java.util.function.*;

/** 
 *  Adds timed assertion testing to {@link Await}. Unlike Awaitility, this implementation is robust in the face of a
 *  system clock that doesn't satisfy the monotonic non-decreasing assumption. (While rare, this assumption may be
 *  violated when using NTP, and is particularly problematic on macOS.)
 *  <p>
 *  
 *  Timesert is useful when writing and testing network applications and asynchronous systems in general, as events don't
 *  happen instantly. For example, when sending a message you might want to assert that it has been received. But running
 *  an assertion on the receiver immediately following a send in an asynchronous environment will likely fail. Using
 *  Timesert allows for assertions to fail up to a certain point, after which the {@link AssertionError} is percolated to
 *  the caller and the test case fails. This way Timesert allows you to write efficient, reproducible assertions without
 *  resorting to {@link Thread#sleep()}.
 */
public final class Timesert {
  private int waitMillis;
  
  private int intervalMillis = Await.DEF_INTERVAL;
  
  private Timesert(int waitMillis) {
    this.waitMillis = waitMillis;
  }
  
  public static Timesert wait(int waitMillis) {
    return new Timesert(waitMillis);
  }
  
  public Timesert withScale(int scale) {
    waitMillis *= scale;
    return this;
  }
  
  public Timesert withIntervalMillis(int intervalMillis) {
    this.intervalMillis = intervalMillis;
    return this;
  }

  private static BooleanSupplier isAsserted(Runnable assertion) {
    return () -> {
      try {
        assertion.run();
        return true;
      } catch (AssertionError e) {
        return false;
      }
    };
  }
  
  public void until(Runnable assertion) {
    try {
      if (! Await.bounded(waitMillis, intervalMillis, isAsserted(assertion))) {
        assertion.run();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
  
  public void untilTrue(BooleanSupplier test) {
    until(fromBoolean(test));
  }
  
  private static Runnable fromBoolean(BooleanSupplier test) {
    return () -> {
      if (! test.getAsBoolean()) throw new AssertionError();
    };
  }
}
