package com.obsidiandynamics.socketx;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.time.*;

import com.obsidiandynamics.socketx.util.*;

/**
 *  The abstract definition of a WebSocket endpoint.
 */
public interface XEndpoint extends AutoCloseable {
  /**
   *  Obtains the context associated with this endpoint.
   *  
   *  @param <T> The context type.
   *  @return The context, if set.
   */
  <T> T getContext();
  
  /**
   *  Associates an arbitrary context object with this endpoint.
   *  
   *  @param context The context to associate this endpoint with.
   */
  void setContext(Object context);
  
  /**
   *  Asynchronously sends a text frame.
   *  
   *  @param payload The payload.
   */
  default void send(String payload) {
    send(payload, null);
  }
  
  /**
   *  Asynchronously sends a text frame.
   *  
   *  @param payload The payload.
   *  @param callback Optional callback, invoked when the send completes (or fails).
   */
  void send(String payload, XSendCallback callback);
  
  /**
   *  Asynchronously sends a binary frame.
   *  
   *  @param payload The payload.
   */
  default void send(ByteBuffer payload) {
    send(payload, null);
  }

  /**
   *  Asynchronously sends a binary frame.
   *  
   *  @param payload The payload.
   *  @param callback Optional callback, invoked when the send completes (or fails).
   */
  void send(ByteBuffer payload, XSendCallback callback);
  
  /**
   *  Flushing the underlying stream. Depending on the implementation, this method may block.
   *  
   *  @throws IOException If an I/O error occurs.
   */
  void flush() throws IOException;

  /**
   *  Asynchronously sends a ping frame.
   */
  void sendPing();
  
  /**
   *  Determines whether the underlying connection is open.
   *  
   *  @return True if the connection is open.
   */
  boolean isOpen();
  
  /**
   *  Obtains the socket address of the peer endpoint.
   *  
   *  @return The remote socket address.
   */
  InetSocketAddress getRemoteAddress();
  
  /**
   *  Obtains the send backlog - the number of messages queued for sending but yet to be confirmed.
   *  
   *  @return The number of backlogged messages.
   */
  long getBacklog();

  /**
   *  Terminate the connection without sending the standard WebSocket close opcode.
   *
   *  @throws IOException If an I/O error occurs.
   */
  void terminate() throws IOException;
  
  /**
   *  Obtains the timestamp of the last send or receive activity.
   *  
   *  @return The last activity time.
   */
  long getLastActivityTime();
  
  /**
   *  Obtains the last activity time as a {@link ZonedDateTime}.
   *  
   *  @return The last activity time.
   */
  default ZonedDateTime getLastActivityZoned() {
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(getLastActivityTime()), ZoneOffset.systemDefault());
  }
  
  /**
   *  Awaits the closure of the underlying channel, which implies that the close frame handshake
   *  would have been performed.
   *  
   *  @param waitMillis The number of milliseconds to wait.
   *  @return True if the endpoint was closed.
   *  @throws InterruptedException If the thread was interrupted.
   */
  default boolean awaitClose(int waitMillis) throws InterruptedException {
    return Await.bounded(waitMillis, () -> ! isOpen());
  }
  
  /**
   *  The default {@code toString()} implementation.
   *  
   *  @param endpoint The endpoint.
   *  @return The default {@code toString()}} representation.
   */
  static String defaultToString(XEndpoint endpoint) {
    return endpoint.getClass().getSimpleName() + 
        " [remote=" + endpoint.getRemoteAddress() + ", lastActivity=" + endpoint.getLastActivityZoned() + "]";
  }
}
