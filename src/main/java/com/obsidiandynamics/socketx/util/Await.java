package com.obsidiandynamics.socketx.util;

import java.util.concurrent.*;
import java.util.function.*;

/**
 *  Utility for awaiting a specific condition. Unlike Awaitility, this implementation
 *  is robust in the face of non-monotonically increasing clocks.
 */
public final class Await {
  public static final int DEF_INTERVAL = 1;
  
  private Await() {}
  
  public static void perpetual(BooleanSupplier test) throws InterruptedException {
    bounded(Integer.MAX_VALUE, DEF_INTERVAL, test);
  }
  
  public static void perpetual(int intervalMillis, BooleanSupplier test) throws InterruptedException {
    bounded(Integer.MAX_VALUE, intervalMillis, test);
  }
  
  public static void boundedTimeout(int waitMillis, BooleanSupplier test) throws InterruptedException, TimeoutException {
    boundedTimeout(waitMillis, DEF_INTERVAL, test);
  }
  
  public static boolean bounded(int waitMillis, BooleanSupplier test) throws InterruptedException {
    return bounded(waitMillis, DEF_INTERVAL, test);
  }
  
  public static void boundedTimeout(int waitMillis, 
                                    int intervalMillis, 
                                    BooleanSupplier test) throws InterruptedException, TimeoutException {
    if (! bounded(waitMillis, intervalMillis, test)) {
      throw new TimeoutException(String.format("Timed out after %,d ms", waitMillis));
    }
  }
  
  public static boolean bounded(int waitMillis, int intervalMillis, BooleanSupplier test) throws InterruptedException {
    final long maxWait = System.nanoTime() + waitMillis * 1_000_000l;
    boolean result;
    for (;;) {
      result = test.getAsBoolean();
      if (result) {
        return true;
      } else {
        final long now = System.nanoTime();
        final long remainingNanos = maxWait - now;
        if (remainingNanos < 0) {
          return false;
        } else {
          final long sleepMillis = Math.max(0, Math.min(remainingNanos / 1_000_000l, intervalMillis));
          if (sleepMillis != 0) {
            Thread.sleep(sleepMillis);
          }
        }
      }
    }
  }
}
