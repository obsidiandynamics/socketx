package com.obsidiandynamics.socketx.netty;

import com.obsidiandynamics.socketx.*;

import io.netty.bootstrap.*;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import io.netty.channel.socket.nio.*;
import io.netty.handler.logging.*;
import io.netty.handler.ssl.*;

public final class NettyServer implements XServer<NettyEndpoint> {
  private final XServerConfig config;
  private final NettyEndpointManager manager;
  private final EventLoopGroup bossGroup;
  private final EventLoopGroup workerGroup;
  private final XEndpointScanner<NettyEndpoint> scanner;
  
  private final Channel channel;
  private final Channel httpsChannel;
  
  private NettyServer(XServerConfig config, XEndpointListener<? super NettyEndpoint> listener) throws Exception {
    if (config.servlets.length != 0) {
      throw new UnsupportedOperationException("Servlets are not supported by " + NettyServer.class.getSimpleName());
    }
    this.config = config;
    
    scanner = new XEndpointScanner<>(config.scanIntervalMillis, config.pingIntervalMillis);
    manager = new NettyEndpointManager(scanner, config, listener);
    final int eventLoopThreads = NettyAtts.EVENT_LOOP_THREADS.get(config.attributes);
    bossGroup = new NioEventLoopGroup(eventLoopThreads);
    workerGroup = new NioEventLoopGroup();
    
    channel = createChannel(bossGroup, workerGroup, manager, config.path, null, config.idleTimeoutMillis, config.port);
    
    if (config.httpsPort != 0) {
      final SslContext sslContext = new JdkSslContext(config.sslContextProvider.getSSLContext(), false, ClientAuth.NONE);
      httpsChannel = createChannel(bossGroup, workerGroup, manager, config.path, sslContext,
                                   config.idleTimeoutMillis, config.httpsPort);
    } else {
      httpsChannel = null;
    }
  }
  
  private static Channel createChannel(EventLoopGroup bossGroup, EventLoopGroup workerGroup, 
                                       NettyEndpointManager manager, String path, SslContext sslContext, 
                                       int idleTimeoutMillis, int port) throws InterruptedException {
    return new ServerBootstrap()
    .group(bossGroup, workerGroup)
    .channel(NioServerSocketChannel.class)
    .handler(new LoggingHandler(LogLevel.INFO))
    .childOption(ChannelOption.SO_REUSEADDR, true)
    .childHandler(new WebSocketServerInitializer(manager, path, sslContext, idleTimeoutMillis))
    .bind(port)
    .sync()
    .channel();
  }
  
  @Override
  public void close() throws Exception {
    scanner.closeEndpoints(60_000);
    scanner.close();
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
    channel.closeFuture().sync();
    if (httpsChannel != null) httpsChannel.closeFuture().sync();
  }

  @Override
  public NettyEndpointManager getEndpointManager() {
    return manager;
  }
  
  @Override
  public XServerConfig getConfig() {
    return config;
  }
  
  public static XServerFactory<NettyEndpoint> factory() {
    return NettyServer::new;
  }
}