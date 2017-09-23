package com.obsidiandynamics.socketx;

public interface XServer<E extends XEndpoint> extends AutoCloseable {
  XEndpointManager<E> getEndpointManager();
  
  XServerConfig getConfig();
}
