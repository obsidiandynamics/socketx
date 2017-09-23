package com.obsidiandynamics.socketx;

import org.junit.Test;

import com.obsidiandynamics.indigo.util.*;

import junit.framework.*;

public final class XMappedServletTest {
  @Test
  public void testToString() {
    TestSupport.assertToString(new XMappedServlet("/test", "health", HealthServlet.class));
  }

  @Test
  public void testGeneratedName() {
    final XMappedServlet s = new XMappedServlet("/test", HealthServlet.class);
    TestCase.assertTrue("name=" + s.getName(), s.getName().startsWith(HealthServlet.class.getSimpleName() + "_"));
  }

  @Test
  public void testExplicitName() {
    final XMappedServlet s = new XMappedServlet("/test", "health", HealthServlet.class);
    TestCase.assertEquals("health", s.getName());
  }

  @Test
  public void testClass() {
    final XMappedServlet s = new XMappedServlet("/test", "health", HealthServlet.class);
    TestCase.assertEquals(HealthServlet.class, s.getServletClass());
  }

  @Test
  public void testPath() {
    final XMappedServlet s = new XMappedServlet("/test", "health", HealthServlet.class);
    TestCase.assertEquals("/test", s.getPath());
  }
}
