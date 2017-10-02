package com.obsidiandynamics.socketx.util;

import java.util.concurrent.*;
import java.util.function.*;

/**
 *  Utility for awaiting a specific condition. The utility methods block the calling thread until a 
 *  certain condition, described by the specified {@link BooleanSupplier} evaluates to {@code true}.<p>
 *  
 *  There are variations of the blocking methods - some return a {@code boolean}, indicating whether 
 *  the condition has been satisfied with the allotted time frame, while others throw a 
 *  {@link TimeoutException}. You can specify an upper bound on how long to wait for, as well as the 
 *  checking interval (which otherwise defaults to 1 ms). All times are in milliseconds.
 */
public final class Await {
  public static final int DEF_INTERVAL = 1;
  
  private Await() {}
  
  /**
   *  A variant of {@link #perpetual(int, BooleanSupplier)}, using an interval of 
   *  {@link #DEF_INTERVAL} between successive tests.
   *  
   *  @param condition The condition to await.
   *  @throws InterruptedException If the thread was interrupted while waiting for the condition.
   */
  public static void perpetual(BooleanSupplier condition) throws InterruptedException {
    bounded(Integer.MAX_VALUE, DEF_INTERVAL, condition);
  }
  
  /**
   *  Blocks indefinitely until the condition specified by the given {@link BooleanSupplier}
   *  is satisfied.
   *  
   *  @param intervalMillis The interval between successive tests, in milliseconds.
   *  @param condition The condition to await.
   *  @throws InterruptedException If the thread was interrupted while waiting for the condition.
   */
  public static void perpetual(int intervalMillis, BooleanSupplier condition) throws InterruptedException {
    bounded(Integer.MAX_VALUE, intervalMillis, condition);
  }
  
  /**
   *  A variant of {@link #boundedTimeout(int, int, BooleanSupplier)}, using an interval of 
   *  {@link #DEF_INTERVAL} between successive tests.
   *  
   *  @param waitMillis The upper bound on the wait time, in milliseconds.
   *  @param condition The condition to await.
   *  @throws InterruptedException If the thread was interrupted while waiting for the condition.
   *  @throws TimeoutException If the condition wasn't satisfied within the given time frame.
   */
  public static void boundedTimeout(int waitMillis, BooleanSupplier condition) throws InterruptedException, TimeoutException {
    boundedTimeout(waitMillis, DEF_INTERVAL, condition);
  }
  
  /**
   *  A variant of {@link #bounded(int, int, BooleanSupplier)}, using an interval of 
   *  {@link #DEF_INTERVAL} between successive tests.
   *  
   *  @param waitMillis The upper bound on the wait time, in milliseconds.
   *  @param condition The condition to await.
   *  @return The final result of the tested condition; if {@code false} then this invocation has timed out.
   *  @throws InterruptedException If the thread was interrupted while waiting for the condition.
   */
  public static boolean bounded(int waitMillis, BooleanSupplier condition) throws InterruptedException {
    return bounded(waitMillis, DEF_INTERVAL, condition);
  }
  
  /**
   *  Awaits a condition specified by the given {@link BooleanSupplier}, blocking until the condition
   *  evaluates to {@code true}. If the condition isn't satisfied within the alloted time frame, a 
   *  {@link TimeoutException} is thrown.
   *  
   *  @param waitMillis The upper bound on the wait time, in milliseconds.
   *  @param intervalMillis The interval between successive tests, in milliseconds.
   *  @param condition The condition to await.
   *  @throws InterruptedException If the thread was interrupted while waiting for the condition.
   *  @throws TimeoutException If the condition wasn't satisfied within the given time frame.
   */
  public static void boundedTimeout(int waitMillis, 
                                    int intervalMillis, 
                                    BooleanSupplier condition) throws InterruptedException, TimeoutException {
    if (! bounded(waitMillis, intervalMillis, condition)) {
      throw new TimeoutException(String.format("Timed out after %,d ms", waitMillis));
    }
  }
  
  /**
   *  Awaits a condition specified by the given {@link BooleanSupplier}, blocking until the condition
   *  evaluates to {@code true}.
   *  
   *  @param waitMillis The upper bound on the wait time, in milliseconds.
   *  @param intervalMillis The interval between successive tests, in milliseconds.
   *  @param condition The condition to await.
   *  @return The final result of the tested condition; if {@code false} then this invocation has timed out.
   *  @throws InterruptedException If the thread was interrupted while waiting for the condition.
   */
  public static boolean bounded(int waitMillis, int intervalMillis, BooleanSupplier condition) throws InterruptedException {
    final long maxWait = System.nanoTime() + waitMillis * 1_000_000l;
    boolean result;
    for (;;) {
      result = condition.getAsBoolean();
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
