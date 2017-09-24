package com.obsidiandynamics.socketx.jetty;

import com.obsidiandynamics.socketx.attribute.*;

public final class JettyAtts {
  public static final Attribute<Integer> MIN_THREADS = new Attribute<Integer>("socketx.jetty.minThreads")
      .withMin(Constant.of(1))
      .withDefault(Constant.of(8));
  
  public static final Attribute<Integer> MAX_THREADS = new Attribute<Integer>("socketx.jetty.maxThreads")
      .withMin(atts -> MIN_THREADS.get(atts) + 4)
      .withDefault(Constant.of(128));
  
  private JettyAtts() {}
}
