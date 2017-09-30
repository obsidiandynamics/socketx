package com.obsidiandynamics.socketx;

import java.net.*;
import java.util.*;

import com.obsidiandynamics.socketx.util.*;

/**
 *  A conduit for opening and maintaining client connections.
 *
 *  @param <E> The endpoint type.
 */
public interface XClient<E extends XEndpoint> extends AutoCloseable {
  /**
   *  Opens a new client connection, returning when the connection succeeds.
   *  
   *  @param uri The endpoint URI. (ws://... or wss:/...)
   *  @param listener The endpoint listener.
   *  @return The connection object.
   *  @throws Exception If an error occurs.
   */
  E connect(URI uri, XEndpointListener<? super E> listener) throws Exception;
  
  /**
   *  Obtains the connected endpoints.
   *  
   *  @return The connected endpoints.
   */
  Collection<E> getEndpoints();
  
  /**
   *  Obtains the underlying configuration.
   *  
   *  @return The client configuration.
   */
  XClientConfig getConfig();
  
  /**
   *  Awaits the drainage of connections, blocks until no more open
   *  connections remain.
   *  
   *  @throws InterruptedException If this thread was interrupted.
   */
  default void drain() throws InterruptedException {
    Await.perpetual(getEndpoints()::isEmpty);
  }
}
