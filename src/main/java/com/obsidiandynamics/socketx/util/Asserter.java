package com.obsidiandynamics.socketx.util;

import java.util.function.*;

/**
 *  Adds timed assertion testing to {@link Await}.
 */
public final class Asserter {
  private int waitMillis;
  
  private int intervalMillis = Await.DEF_INTERVAL;
  
  private Asserter(int waitMillis) {
    this.waitMillis = waitMillis;
  }
  
  public static Asserter wait(int waitMillis) {
    return new Asserter(waitMillis);
  }
  
  public Asserter withScale(int scale) {
    waitMillis *= scale;
    return this;
  }
  
  public Asserter withIntervalMillis(int intervalMillis) {
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
