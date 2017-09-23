package com.obsidiandynamics.socketx.util;

import static org.junit.Assert.*;

import java.net.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

import com.obsidiandynamics.socketx.util.URIBuilder.*;

public final class URIBuilderTest {
  private static Ports getPorts() {
    return new Ports(8080, 8443);
  }

  @Test
  public void testPortsToString() {
    TestSupport.assertToString(getPorts());
  }

  @Test
  public void testHttp() throws URISyntaxException {
    final URI uri = URIBuilder.create()
        .withHttps(false)
        .withHost("pound")
        .withPath("/dog")
        .withPortProvider(getPorts())
        .build();
    assertEquals(new URI("http://pound:8080/dog"), uri);
  }

  @Test
  public void testHttps() throws URISyntaxException {
    final URI uri = URIBuilder.create()
        .withHttps(true)
        .withHost("pound")
        .withPath("/dog")
        .withPortProvider(getPorts())
        .build();
    assertEquals(new URI("https://pound:8443/dog"), uri);
  }

  @Test
  public void testWS() throws URISyntaxException {
    final URI uri = URIBuilder.create()
        .withWebSocket(true)
        .withHttps(false)
        .withHost("pound")
        .withPath("/dog")
        .withPortProvider(getPorts())
        .build();
    assertEquals(new URI("ws://pound:8080/dog"), uri);
  }

  @Test
  public void testWSS() throws URISyntaxException {
    final URI uri = URIBuilder.create()
        .withWebSocket(true)
        .withHttps(true)
        .withHost("pound")
        .withPath("/dog")
        .withPortProvider(getPorts())
        .build();
    assertEquals(new URI("wss://pound:8443/dog"), uri);
  }

  @Test
  public void testFixedPort() throws URISyntaxException {
    final URI uri = URIBuilder.create()
        .withHttps(false)
        .withHost("pound")
        .withPath("/dog")
        .withPort(80)
        .build();
    assertEquals(new URI("http://pound:80/dog"), uri);
  }

  @Test(expected=URIBuilderException.class)
  public void testBadURI() throws URISyntaxException {
    URIBuilder.create()
        .withHttps(false)
        .withHost("\\")
        .withPath("/dog")
        .withPort(-1)
        .build();
  }
}
