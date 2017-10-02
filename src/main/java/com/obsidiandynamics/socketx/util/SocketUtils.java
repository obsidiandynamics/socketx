package com.obsidiandynamics.socketx.util;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.*;

import org.slf4j.*;

import com.obsidiandynamics.indigo.util.*;

/**
 *  Utilities for working with TCP sockets.
 */
public final class SocketUtils {
  private static Logger LOG = LoggerFactory.getLogger(SocketUtils.class);
  
  private static int DEF_MAX_PORT = 49151;
  private static int DEF_PORT_DRAIN_INTERVAL_MILLIS = 100;
  
  private static int DEF_AWAIT_MILLIS = 120_000;
  
  private SocketUtils() {}
  
  /**
   *  Returns a {@link Timesert} builder primed with the default timeout of
   *  {@link #DEF_AWAIT_MILLIS}.
   *  
   *  @return The {@link Timesert} builder.
   */
  public static Timesert await() {
    return Timesert.wait(DEF_AWAIT_MILLIS);
  }
  
  /**
   *  Thrown if the port range of {@link #getAvailablePort} was exhausted while
   *  looking for an available port.
   */
  public static final class NoAvailablePortsException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    NoAvailablePortsException(String m) { super(m); }
  }
  
  /**
   *  Variant of {@link #getAvailablePort(int, int)} that uses {@link #DEF_MAX_PORT}
   *  as the upper bound.
   *  
   *  @param preferredPort The port to try first. Each subsequent attempt is an increment
   *                       of the prior.
   *  @return The available port.
   *  @exception NoAvailablePortsException If the port range was exhausted.
   */
  public static int getAvailablePort(int preferredPort) {
    return getAvailablePort(preferredPort, DEF_MAX_PORT);
  }
  
  /**
   *  Searches for free (unbound) ports on the local machine, starting with the 
   *  {@code preferredPort}, and up to the {@code maxPort}, inclusive. This method 
   *  returns the first available port in the given range if one was found in a single 
   *  complete pass. Alternatively, a {@link NoAvailablePortsException} is thrown if 
   *  no unbound ports are available. Typically, this method is used when you need a 
   *  spare port to bind to and where your application may have a preference for a 
   *  specific port, but can still function correctly if a different port is assigned. 
   *  This is often the case for testing scenarios.
   *  
   *  @param preferredPort The port to try first. Each subsequent attempt is an increment
   *                       of the prior.
   *  @param maxPort The highest port that may be tried.
   *  @return The available port.
   *  @exception NoAvailablePortsException If the port range was exhausted.
   */
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
  
  /**
   *  Tests whether a single port is bound on the local machine. Behind the scenes, the method
   *  attempts to bind on the port and, if successful, sets the {@code SO_REUSEADDR} socket option
   *  before yielding the port, so that it may be used immediately.
   *  
   *  @param port The port to check.
   *  @return True if the port is available for binding.
   */
  public static boolean isLocalPortAvailable(int port) {
    try (ServerSocket ss = new ServerSocket(port, 1, Inet4Address.getByAddress(new byte[4]))) {
      ss.setReuseAddress(true);
      return true;
    } catch (IOException e) {}
    return false;
  }
  
  /**
   *  Used to query the number of uses of a given port, including the number of open socket connections, 
   *  sockets in a {@code CLOSE_WAIT} state, as well as {@code LISTEN}. Effectively, this method counts 
   *  the number of times a port appears in the output of a {@code netstat} command. Presently, this 
   *  method relies on system utilities {@code netstat}, {@code grep} and {@code wc}, and can therefore 
   *  only be used on a *NIX operating system, such as Linux, BSD, macOS, etc.
   *  
   *  @param port The port to test
   *  @return The number of current uses of this port.
   *  @exception ProcessExecutionException If the command failed to execute.
   */
  public static int getPortUseCount(int port) {
    final String cmdTemplate = "which netstat > /dev/null && netstat -an | grep \"[\\.|:]%d \" | wc -l";
    return Integer.parseInt(execute(String.format(cmdTemplate, port)).trim());
  }
  
  /**
   *  Thrown if an executed process terminated with a non-zero exit code.
   */
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
  
  /**
   *  Variant of {@link #drainPort(int, int, int)} that waits for 
   *  {@link #DEF_PORT_DRAIN_INTERVAL_MILLIS} between successive checks.
   *  
   *  @param port The port to drain.
   *  @param maxUseCount The maximum number of uses.
   *  @throws InterruptedException If the thread was interrupted while draining.
   */
  public static void drainPort(int port, int maxUseCount) throws InterruptedException {
    drainPort(port, maxUseCount, DEF_PORT_DRAIN_INTERVAL_MILLIS);
  }
  
  /**
   *  Blocks the calling thread until the number of uses of the given port reaches or drops 
   *  below {@code maxUseCount}, effectively draining the port of open connections. 
   *  
   *  @param port The port to drain.
   *  @param maxUseCount The maximum number of uses.
   *  @param drainIntervalMillis Introduces waits between successive checks.
   *  @throws InterruptedException If the thread was interrupted while draining.
   */
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
