package com.obsidiandynamics.socketx;

import static junit.framework.TestCase.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class XServerConfigTest {
  @Test
  public void testToString() {
    Assertions.assertToStringOverride(new XServerConfig());
  }

  @Test
  public void testPort() {
    assertEquals(9090, new XServerConfig().withPort(9090).port);
  }

  @Test
  public void testHttpsPort() {
    assertEquals(9443, new XServerConfig().withHttpsPort(9443).httpsPort);
  }

  @Test
  public void testPath() {
    assertEquals("/foo/bar", new XServerConfig().withPath("/foo/bar").path);
  }

  @Test
  public void testPingInterval() {
    assertEquals(3000, new XServerConfig().withPingInterval(3000).pingIntervalMillis);
  }

  @Test
  public void testServlets() {
    final XMappedServlet[] servlets = new XMappedServlet[] { new XMappedServlet("/health", "health", HealthServlet.class) };
    assertEquals(servlets, new XServerConfig().withServlets(servlets).servlets);
  }
}
