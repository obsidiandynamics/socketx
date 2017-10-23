package com.obsidiandynamics.socketx.util;

import java.util.concurrent.atomic.*;

import org.junit.Test;

import com.obsidiandynamics.indigo.util.*;

import junit.framework.*;

public final class TimesertTest {
  @Test
  public void testPass() {
    Timesert.wait(10).withScale(2).withIntervalMillis(1).until(() -> {});
  }
  
  @Test
  public void testPassBoolean() {
    Timesert.wait(10).withScale(2).withIntervalMillis(1).untilTrue(() -> true);
  }
  
  @Test
  public void testFail() {
    final String message = "Boom";
    try {
      Timesert.wait(20).withIntervalMillis(1).until(() -> { throw new AssertionError(message); });
      TestCase.fail("AssertionError not thrown");
    } catch (AssertionError e) {
      TestCase.assertEquals(message, e.getMessage());
    }
  }
  
  @Test
  public void testFailBoolean() {
    try {
      Timesert.wait(20).withIntervalMillis(1).untilTrue(() -> false);
      TestCase.fail("AssertionError not thrown");
    } catch (AssertionError e) {}
  }
  
  @Test
  public void testPartialFail() {
    final AtomicInteger calls = new AtomicInteger();
    Timesert.wait(0).withIntervalMillis(1).until(() -> { 
      if (calls.getAndIncrement() == 0) {
        TestSupport.sleep(1);
        throw new AssertionError("Boom"); 
      }
    });
  }
  
  @Test
  public void testInterrupted() {
    try {
      Timesert.wait(20).untilTrue(() -> {
        Thread.currentThread().interrupt();
        return false;
      });
    } catch (AssertionError ae) {
    } finally {
      TestCase.assertTrue(Thread.interrupted());
    }
  }
}
