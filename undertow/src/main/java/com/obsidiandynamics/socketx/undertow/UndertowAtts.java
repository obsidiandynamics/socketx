package com.obsidiandynamics.socketx.undertow;

import com.obsidiandynamics.socketx.attribute.*;

public final class UndertowAtts {
  public static final Attribute<Integer> IO_THREADS = new Attribute<Integer>("socketx.undertow.ioThreads")
      .withMin(Constant.of(1))
      .withDefault(Constant.of(Math.max(2, Runtime.getRuntime().availableProcessors())));
  
  public static final Attribute<Integer> CORE_TASK_THREADS = new Attribute<Integer>("socketx.undertow.coreTaskThreads")
      .withMin(Constant.of(1))
      .withDefault(atts -> IO_THREADS.get(atts) * 8);
  
  public static final Attribute<Integer> MAX_TASK_THREADS = new Attribute<Integer>("socketx.undertow.maxTaskThreads")
      .withMin(Constant.of(1))
      .withDefault(CORE_TASK_THREADS::get);

  public static final Attribute<Integer> BUFFER_SIZE = new Attribute<Integer>("socketx.undertow.bufferSize")
      .withMin(Constant.of(1))
      .withDefault(Constant.of(1024));

  public static final Attribute<Boolean> DIRECT_BUFFERS = new Attribute<Boolean>("socketx.undertow.directBuffers")
      .withDefault(Constant.of(false));
  
  private UndertowAtts() {}
}
