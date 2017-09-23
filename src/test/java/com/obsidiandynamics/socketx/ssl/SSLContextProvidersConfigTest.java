package com.obsidiandynamics.socketx.ssl;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

import com.obsidiandynamics.yconf.*;

public final class SSLContextProvidersConfigTest {
  @Test
  public void test() throws IOException {
    final SSLContextProvider[] providers = new MappingContext()
        .withParser(new SnakeyamlParser())
        .fromStream(SSLContextProvidersConfigTest.class.getClassLoader().getResourceAsStream("ssl-context-providers.yaml"))
        .map(SSLContextProvider[].class);
    assertNotNull(providers);
    assertEquals(4, providers.length);
  }
}
