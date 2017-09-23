package com.obsidiandynamics.socketx;

import java.nio.*;

import org.junit.*;
import org.mockito.*;

import com.obsidiandynamics.socketx.XEndpointLambdaListener.*;

/**
 *  Allows for incremental composition of a {@link XEndpointListener} by including
 *  the necessary Lambda functions corresponding to the callbacks of interest.<p>
 *  
 *  This is a functional analogue of implementing the {@link XEndpointListener} interface, 
 *  which would otherwise require implementing each and every method.<p>
 *  
 *  As an alternative, one can subclass {@link XEndpointLambdaListener} directly, and 
 *  override only the necessary callback methods.
 */
public final class XEndpointLambdaListenerTest {
  private XEndpointLambdaListener<XEndpoint> listener;
  
  private XEndpoint testEndpoint;
  
  @Before
  public void setup() {
    listener = new XEndpointLambdaListener<>();
    testEndpoint = mock(XEndpoint.class);
  }

  @Test
  public void testOnConnect() {
    @SuppressWarnings("unchecked")
    final OnConnect<XEndpoint> handler = mock(OnConnect.class);
    listener.onConnect(testEndpoint);
    listener.onConnect(handler);
    listener.onConnect(testEndpoint);
    Mockito.verify(handler).onConnect(Mockito.eq(testEndpoint));
  }

  @Test
  public void testOnText() {
    @SuppressWarnings("unchecked")
    final OnText<XEndpoint> handler = mock(OnText.class);
    listener.onText(testEndpoint, "message");
    listener.onText(handler);
    listener.onText(testEndpoint, "message");
    Mockito.verify(handler).onText(Mockito.eq(testEndpoint), Mockito.eq("message"));
  }

  @Test
  public void testOnBinary() {
    @SuppressWarnings("unchecked")
    final OnBinary<XEndpoint> handler = mock(OnBinary.class);
    listener.onBinary(testEndpoint, ByteBuffer.wrap("binary".getBytes()));
    listener.onBinary(handler);
    listener.onBinary(testEndpoint, ByteBuffer.wrap("binary".getBytes()));
    Mockito.verify(handler).onBinary(Mockito.eq(testEndpoint), Mockito.eq(ByteBuffer.wrap("binary".getBytes())));
  }

  @Test
  public void testOnPing() {
    @SuppressWarnings("unchecked")
    final OnPing<XEndpoint> handler = mock(OnPing.class);
    listener.onPing(testEndpoint, ByteBuffer.wrap("binary".getBytes()));
    listener.onPing(handler);
    listener.onPing(testEndpoint, ByteBuffer.wrap("binary".getBytes()));
    Mockito.verify(handler).onPing(Mockito.eq(testEndpoint), Mockito.eq(ByteBuffer.wrap("binary".getBytes())));
  }

  @Test
  public void testOnPong() {
    @SuppressWarnings("unchecked")
    final OnPong<XEndpoint> handler = mock(OnPong.class);
    listener.onPong(testEndpoint, ByteBuffer.wrap("binary".getBytes()));
    listener.onPong(handler);
    listener.onPong(testEndpoint, ByteBuffer.wrap("binary".getBytes()));
    Mockito.verify(handler).onPong(Mockito.eq(testEndpoint), Mockito.eq(ByteBuffer.wrap("binary".getBytes())));
  }

  @Test
  public void testOnDisconnect() {
    @SuppressWarnings("unchecked")
    final OnDisconnect<XEndpoint> handler = mock(OnDisconnect.class);
    listener.onDisconnect(testEndpoint, 10, "reason");
    listener.onDisconnect(handler);
    listener.onDisconnect(testEndpoint, 10, "reason");
    Mockito.verify(handler).onDisconnect(Mockito.eq(testEndpoint), Mockito.eq(10), Mockito.eq("reason"));
  }

  @Test
  public void testOnClose() {
    @SuppressWarnings("unchecked")
    final OnClose<XEndpoint> handler = mock(OnClose.class);
    listener.onClose(testEndpoint);
    listener.onClose(handler);
    listener.onClose(testEndpoint);
    Mockito.verify(handler).onClose(Mockito.eq(testEndpoint));
  }

  @Test
  public void testOnError() {
    @SuppressWarnings("unchecked")
    final OnError<XEndpoint> handler = mock(OnError.class);
    final Exception error = new Exception();
    listener.onError(testEndpoint, error);
    listener.onError(handler);
    listener.onError(testEndpoint, error);
    Mockito.verify(handler).onError(Mockito.eq(testEndpoint), Mockito.eq(error));
  }

  private static <T> T mock(Class<T> cls) {
    return Mockito.mock(cls);
  }
}
