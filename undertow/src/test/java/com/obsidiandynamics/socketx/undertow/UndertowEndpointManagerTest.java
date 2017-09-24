package com.obsidiandynamics.socketx.undertow;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.*;

import org.junit.*;
import org.mockito.*;

import com.obsidiandynamics.socketx.*;
import com.obsidiandynamics.socketx.undertow.UndertowEndpointManager.*;

import io.undertow.websockets.core.*;

public final class UndertowEndpointManagerTest {
  @Test(expected=OptionAssignmentException.class)
  public void testCreateEndpointWithError() throws IOException {
    @SuppressWarnings("unchecked")
    final XEndpointListener<UndertowEndpoint> listener = mock(XEndpointListener.class);
    final UndertowEndpointManager mgr = new UndertowEndpointManager(null, 0, new DerivedEndpointConfig(), listener);
    final WebSocketChannel channel = Mockito.mock(WebSocketChannel.class);
    when(channel.setOption(any(), any())).thenThrow(new IOException("Boom"));
    mgr.createEndpoint(channel);
  }
}
