package com.obsidiandynamics.socketx;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import com.obsidiandynamics.indigo.util.*;
import com.obsidiandynamics.socketx.util.*;

@RunWith(Parameterized.class)
public final class XEndpointScannerTest {
  private static final int REPEAT = 1;
  
  @Parameterized.Parameters
  public static List<Object[]> data() {
    return Arrays.asList(new Object[REPEAT][0]);
  }
  
  private XEndpointScanner<XEndpoint> scanner;
  
  @After
  public void after() throws InterruptedException {
    if (scanner != null) scanner.close();
  }

  @Test
  public void testUnexpectedError() {
    scanner = new XEndpointScanner<XEndpoint>(1, 0);
    final XEndpoint endpoint = mock(XEndpoint.class);
    when(endpoint.isOpen()).thenThrow(new RuntimeException("boom"));
    scanner.addEndpoint(endpoint);
    SocketTestSupport.await().until(() -> {
      verify(endpoint, atLeastOnce()).isOpen();
    });
  }

  @Test
  public void testTerminateDefunct() {
    scanner = new XEndpointScanner<XEndpoint>(1, 0);
    final XEndpoint endpoint = mock(XEndpoint.class);
    final AtomicBoolean isOpen = new AtomicBoolean(true);
    when(endpoint.isOpen()).thenAnswer(invocation -> isOpen.get());
    scanner.addEndpoint(endpoint);
    TestSupport.sleep(10);
    isOpen.set(false);
    SocketTestSupport.await().until(() -> {
      try {
        verify(endpoint, atLeastOnce()).terminate();
      } catch (IOException e) {
        fail(e.getMessage());
      }
    });
  }

  @Test
  public void testPing() {
    scanner = new XEndpointScanner<XEndpoint>(1, 10);
    final XEndpoint endpoint = mock(XEndpoint.class);
    when(endpoint.isOpen()).thenReturn(true);
    when(endpoint.getLastActivityTime()).thenReturn(System.currentTimeMillis());
    scanner.addEndpoint(endpoint);
    SocketTestSupport.await().until(() -> {
      verify(endpoint, atLeastOnce()).sendPing();
    });
  }
  
  @Test
  public void testAddGetRemove() {
    scanner = new XEndpointScanner<XEndpoint>(1, 1);
    final XEndpoint endpoint = mock(XEndpoint.class);
    scanner.addEndpoint(endpoint);
    assertEquals(1, scanner.getEndpoints().size());
    assertTrue(scanner.getEndpoints().contains(endpoint));
    scanner.removeEndpoint(endpoint);
    assertEquals(0, scanner.getEndpoints().size());
  }
  
  @Test
  public void testCloseEndpoint() throws Exception {
    scanner = new XEndpointScanner<XEndpoint>(1, 1);
    final XEndpoint endpoint = mock(XEndpoint.class);
    scanner.addEndpoint(endpoint);
    scanner.closeEndpoints(0);
    verify(endpoint).close();
    verify(endpoint).awaitClose(eq(0));
  }
}
