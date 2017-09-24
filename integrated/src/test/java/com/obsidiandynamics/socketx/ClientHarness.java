package com.obsidiandynamics.socketx;

import java.util.concurrent.atomic.*;

public abstract class ClientHarness extends BaseHarness {
  public final AtomicBoolean connected = new AtomicBoolean();
  public final AtomicBoolean closed = new AtomicBoolean();
}
