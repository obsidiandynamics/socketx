package com.obsidiandynamics.socketx.netty;

import java.util.*;
import java.util.concurrent.*;

import com.obsidiandynamics.socketx.*;

import io.netty.channel.*;

final class NettyEndpointManager implements XEndpointManager<NettyEndpoint> {
  private final XEndpointConfig<?> config;
  
  private final XEndpointListener<? super NettyEndpoint> listener;
  
  private final Map<ChannelId, NettyEndpoint> endpoints = new ConcurrentHashMap<>();
  
  private final XEndpointScanner<NettyEndpoint> scanner;
  
  NettyEndpointManager(XEndpointScanner<NettyEndpoint> scanner, XEndpointConfig<?> config, 
                       XEndpointListener<? super NettyEndpoint> listener) {
    this.scanner = scanner;
    this.config = config;
    this.listener = listener;
  }

  NettyEndpoint createEndpoint(ChannelHandlerContext context) {
    final NettyEndpoint endpoint = new NettyEndpoint(this, context);
    endpoints.put(context.channel().id(), endpoint);
    scanner.addEndpoint(endpoint);
    listener.onConnect(endpoint);
    return endpoint;
  }
  
  NettyEndpoint get(ChannelId channelId) {
    return endpoints.get(channelId);
  }
  
  NettyEndpoint remove(ChannelId channelId) {
    final NettyEndpoint endpoint = endpoints.remove(channelId);
    scanner.removeEndpoint(endpoint);
    return endpoint;
  }
  
  XEndpointListener<? super NettyEndpoint> getListener() {
    return listener;
  }
  
  XEndpointConfig<?> getConfig() {
    return config;
  }

  @Override
  public Collection<NettyEndpoint> getEndpoints() {
    return scanner.getEndpoints();
  }
}
