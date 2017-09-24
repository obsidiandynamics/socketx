package com.obsidiandynamics.socketx;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import com.obsidiandynamics.indigo.util.*;

public abstract class ServerHarness extends BaseHarness {
  public final AtomicLong connected = new AtomicLong();
  public final AtomicLong closed = new AtomicLong();
  
  public abstract List<XEndpoint> getEndpoints();
  
  public abstract void broadcast(List<XEndpoint> endpoints, byte[] payload);
  
  public abstract void broadcast(List<XEndpoint> endpoints, String payload);
  
  public abstract void flush(List<XEndpoint> endpoints) throws IOException;
  
  public abstract void sendPing(XEndpoint endpoint);
  
  protected final void keepAlive(XEndpoint endpoint, AtomicBoolean ping, int idleTimeout) {
    if (idleTimeout != 0) asyncDaemon(() -> {
      while (ping.get()) {
        sendPing(endpoint);
        TestSupport.sleep(idleTimeout / 2);
      }
    }, "PingThread");
  }
  
  private static Thread asyncDaemon(Runnable r, String threadName) {
    final Thread t = new Thread(r, threadName);
    t.setDaemon(true);
    t.start();
    return t;
  }
}
