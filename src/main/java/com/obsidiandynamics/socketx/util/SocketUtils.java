package com.obsidiandynamics.socketx.util;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.*;

import org.slf4j.*;

import com.obsidiandynamics.indigo.util.*;

public final class SocketUtils {
  private static Logger LOG = LoggerFactory.getLogger(SocketUtils.class);
  
  private static int DEF_MAX_PORT = 49151;
  private static int DEF_PORT_DRAIN_INTERVAL_MILLIS = 100;
  
  private static int DEF_AWAIT_MILLIS = 120_000;
  
  private SocketUtils() {}
  
  public static Asserter await() {
    return Asserter.wait(DEF_AWAIT_MILLIS);
  }
  
  public static final class NoAvailablePortsException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    NoAvailablePortsException(String m) { super(m); }
  }
  
  public static int getAvailablePort(int preferredPort) {
    return getAvailablePort(preferredPort, DEF_MAX_PORT);
  }
  
  public static int getAvailablePort(int preferredPort, int maxPort) {
    int port = preferredPort;
    while (port <= maxPort) {
      if (isLocalPortAvailable(port)) {
        return port;
      } else {
        port++;
        LOG.debug("Port {} unavailable for binding; trying {}", preferredPort, port);
      }
    }
    throw new NoAvailablePortsException("No available ports in the range " + preferredPort + " - " + maxPort);
  }
  
  public static boolean isLocalPortAvailable(int port) {
    try (ServerSocket ss = new ServerSocket(port, 1, Inet4Address.getByAddress(new byte[4]))) {
      ss.setReuseAddress(true);
      return true;
    } catch (IOException e) {}
    return false;
  }
  
  public static int getPortUseCount(int port) {
    return Integer.parseInt(execute(String.format("netstat -an | grep %d | wc -l", port)).trim());
  }
  
  public static final class ProcessExecutionException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    ProcessExecutionException(String m) { super(m); }
  }
  
  static String execute(String cmd) {
    final AtomicReference<String> outputHolder = new AtomicReference<>();
    final int exitCode = BashInteractor.execute(cmd, true, outputHolder::set);
    if (exitCode != 0) {
      throw new ProcessExecutionException(String.format("Command '%s' exited with code %d", cmd, exitCode));
    }
    return outputHolder.get();
  }
  
  public static void drainPort(int port, int maxUseCount) throws InterruptedException {
    drainPort(port, maxUseCount, DEF_PORT_DRAIN_INTERVAL_MILLIS);
  }
  
  public static void drainPort(int port, int maxUseCount, int drainIntervalMillis) throws InterruptedException {
    final AtomicBoolean logged = new AtomicBoolean();
    Await.bounded(Integer.MAX_VALUE, drainIntervalMillis, () -> {
      final int useCount = getPortUseCount(port);
      if (useCount > maxUseCount && ! logged.get()) {
        logged.set(true);
        LOG.debug("Port {} at {} connections; draining to {}", port, useCount, maxUseCount);
      }
      return useCount <= maxUseCount;
    });
  }
}
