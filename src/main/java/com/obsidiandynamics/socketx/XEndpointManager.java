package com.obsidiandynamics.socketx;

import java.util.*;

/**
 *  Manages open connections.
 *
 *  @param <E> The endpoint type.
 */
public interface XEndpointManager<E extends XEndpoint> {
  /**
   *  Obtains the connected endpoints.
   *  
   *  @return The connected endpoints.
   */
  Collection<E> getEndpoints();
}
