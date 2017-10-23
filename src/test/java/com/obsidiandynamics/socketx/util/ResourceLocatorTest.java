package com.obsidiandynamics.socketx.util;

import static org.junit.Assert.*;

import java.io.*;
import java.net.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class ResourceLocatorTest {
  private InputStream in;
  
  @After
  public void after() throws IOException {
    if (in != null) in.close();
  }
  
  @Test
  public void testConformance() throws Exception {
    Assertions.assertUtilityClassWellDefined(ResourceLocator.class);
  }
  
  @Test
  public void testFile() throws FileNotFoundException, URISyntaxException {
    in = ResourceLocator.asStream(new URI("file://src/test/resources/resource-locator.test"));
    assertNotNull(in);
  }
  
  @Test
  public void testCP() throws FileNotFoundException, URISyntaxException {
    in = ResourceLocator.asStream(new URI("cp://resource-locator.test"));
    assertNotNull(in);
  }
  
  @Test
  public void testClasspath() throws FileNotFoundException, URISyntaxException {
    in = ResourceLocator.asStream(new URI("cp://resource-locator.test"));
    assertNotNull(in);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testUnsupported() throws FileNotFoundException, URISyntaxException {
    in = ResourceLocator.asStream(new URI("xxx://resource-locator.test"));
  }
}
