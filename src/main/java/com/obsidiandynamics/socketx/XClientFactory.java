package com.obsidiandynamics.socketx;

@FunctionalInterface
public interface XClientFactory<E extends XEndpoint> {
  XClient<E> create(XClientConfig config) throws Exception;
}
