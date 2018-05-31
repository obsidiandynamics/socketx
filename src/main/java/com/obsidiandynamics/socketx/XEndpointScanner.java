package com.obsidiandynamics.socketx;

import java.util.*;
import java.util.concurrent.*;

import org.slf4j.*;

public final class XEndpointScanner<E extends XEndpoint> extends Thread implements AutoCloseable {
  private static final Logger log = LoggerFactory.getLogger(XEndpointScanner.class);
  
  private final int scanIntervalMillis;
  private final int pingIntervalMillis;
  private final Set<E> endpoints = new CopyOnWriteArraySet<>();
  
  private volatile boolean running = true;
  
  public XEndpointScanner(int scanIntervalMillis, int pingIntervalMillis) {
    super(String.format("Scanner[scanInterval=%dms,pingInterval=%dms]", 
                        scanIntervalMillis, pingIntervalMillis));
    this.scanIntervalMillis = scanIntervalMillis;
    this.pingIntervalMillis = pingIntervalMillis;
    start();
  }
  
  @Override
  public void run() {
    while (running) {
      try {
        final long now = System.currentTimeMillis();
        for (E endpoint : endpoints) {
          if (! endpoint.isOpen()) {
            log.debug("Terminating defunct endpoint {}", endpoint);
            endpoint.terminate();
          } else if (pingIntervalMillis != 0) {
            final long lastActivity = endpoint.getLastActivityTime();
            if (now - lastActivity > pingIntervalMillis) {
              log.trace("Pinging {}", endpoint);
              endpoint.sendPing();
            }
          }
        }
      } catch (Exception e) {
        log.error("Unexpected error", e);
      }
      
      try {
        Thread.sleep(scanIntervalMillis);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        continue;
      }
    }
  }
  
  public void addEndpoint(E endpoint) {
    endpoints.add(endpoint);
  }
  
  public void removeEndpoint(E endpoint) {
    endpoints.remove(endpoint);
  }
  
  public Collection<E> getEndpoints() {
    return Collections.unmodifiableSet(endpoints);
  }
  
  @Override
  public void close() throws InterruptedException {
    running = false;
    interrupt();
    join();
  }
  
  public void closeEndpoints(int waitMillis) throws Exception {
    final Collection<E> endpoints = new HashSet<>(this.endpoints);
    for (E endpoint : endpoints) {
      endpoint.close();
    }
    for (E endpoint : endpoints) {
      endpoint.awaitClose(waitMillis);
    }
  }
}
