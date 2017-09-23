package com.obsidiandynamics.socketx.util;

import java.util.*;

/**
 *  Generates parameters for JUnit's {@link org.junit.runners.Parameterized} class
 *  so that the test case can be repeated a set number of times.
 */
public final class TestCycle {
  private TestCycle() {}
  
  public static List<Object[]> once() {
    return timesQuietly(1);
  }

  public static List<Object[]> timesQuietly(int times) {
    return Arrays.asList(new Object[times][0]);
  }
  
  /**
   *  This method assumes that you have an integer variable annotated with 
   *  <code>@Parameter(0)</code>, to which a zero-based run number will be assigned
   *  at the beginning of each iteration.
   *  
   *  @param times The number of times to repeat.
   *  @return The parameter data.
   */
  public static List<Object[]> times(int times) {
    final Object[][] params = new Object[times][1];
    for (int i = 0; i < times; i++) params[i][0] = i;
    return Arrays.asList(params);
  }
}
