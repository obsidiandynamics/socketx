package com.obsidiandynamics.socketx;

import com.obsidiandynamics.await.*;

/**
 *  Listens for incoming connections.
 *
 *  @param <E> The endpoint type.
 */
public interface XServer<E extends XEndpoint> extends AutoCloseable {
  /**
   *  Obtains the endpoint manager.
   *  
   *  @return The endpoint manager.
   */
  XEndpointManager<E> getEndpointManager();
  
  /**
   *  Obtains the underlying configuration.
   *  
   *  @return The client configuration.
   */
  XServerConfig getConfig();
  
  /**
   *  Awaits the drainage of connections, blocks until no more open
   *  connections remain.
   *  
   *  @throws InterruptedException If this thread was interrupted.
   */
  default void drain() throws InterruptedException {
    Await.perpetual(getEndpointManager().getEndpoints()::isEmpty);
  }
}
