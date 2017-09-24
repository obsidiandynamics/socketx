package com.obsidiandynamics.socketx.util;

import static junit.framework.TestCase.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import javax.net.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;
import com.obsidiandynamics.socketx.util.SocketUtils.*;

public final class SocketUtilsTest {
  private volatile ServerSocket socket;
  
  @After
  public void after() throws IOException {
    closeSocket(); 
  }
  
  private void closeSocket() throws IOException {
    if (socket != null) socket.close();
    socket = null;
  }
  
  private void openSocket(int port) throws IOException {
    assertNull(socket);
    socket = ServerSocketFactory.getDefault().createServerSocket(port);
  }
  
  private static int port() {
    return SocketUtils.getAvailablePort(8090);
  }
  
  @Test
  public void testConformance() throws Exception {
    TestSupport.assertUtilityClassWellDefined(SocketUtils.class);
  }
  
  @Test
  public void executeSuccessTest() {
    SocketUtils.execute("netstat -n -p udp");
  }
  
  @Test(expected=ProcessExecutionException.class)
  public void executeFailTest() {
    SocketUtils.execute("abcdefghijk-gibberish");
  }
  
  @Test
  public void testLocalPortAvailable() throws IOException {
    final int port = port();
    assertTrue(SocketUtils.isLocalPortAvailable(port));
    openSocket(port);
    assertFalse(SocketUtils.isLocalPortAvailable(port));
    closeSocket();
    assertTrue(SocketUtils.isLocalPortAvailable(port));
  }
  
  @Test
  public void testGetAvailablePort() throws IOException {
    final int port = port();
    openSocket(port);
    final int nextPort = SocketUtils.getAvailablePort(port);
    Assert.assertNotEquals(port, nextPort);
  }
  
  @Test(expected=NoAvailablePortsException.class)
  public void testGetAvailablePortNoAvailable() throws IOException {
    final int port = port();
    openSocket(port);
    final int nextPort = SocketUtils.getAvailablePort(port, port);
    Assert.assertNotEquals(port, nextPort);
  }
  
  @Test
  public void testDrainPort() throws IOException, InterruptedException, BrokenBarrierException {
    final int port = port();
    openSocket(port);
    final CyclicBarrier barrier = new CyclicBarrier(2);
    new Thread(() -> {
      try {
        barrier.await();
        Thread.sleep(50);
        closeSocket();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();
    barrier.await();
    SocketUtils.drainPort(port, 0, 1);
    SocketUtils.drainPort(port, 0); // second call should do nothing
  }
  
  @Test
  public void testAwait() {
    assertNotNull(SocketUtils.await());
  }
}
