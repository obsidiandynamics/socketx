package com.obsidiandynamics.socketx;

@FunctionalInterface
public interface XServerFactory<E extends XEndpoint> {
  XServer<E> create(XServerConfig config, XEndpointListener<? super E> listener) throws Exception;
}
