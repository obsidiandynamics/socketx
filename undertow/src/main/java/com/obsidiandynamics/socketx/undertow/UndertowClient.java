package com.obsidiandynamics.socketx.undertow;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.net.ssl.*;

import org.xnio.*;
import org.xnio.ssl.*;

import com.obsidiandynamics.socketx.*;

import io.undertow.connector.*;
import io.undertow.protocols.ssl.*;
import io.undertow.server.*;
import io.undertow.websockets.client.*;
import io.undertow.websockets.client.WebSocketClient.*;
import io.undertow.websockets.core.*;

public final class UndertowClient implements XClient<UndertowEndpoint> {
  private final XClientConfig config;
  
  private final XnioWorker worker;
  
  private final XEndpointScanner<UndertowEndpoint> scanner;
  
  private UndertowClient(XClientConfig config, XnioWorker worker, int bufferSize) {
    this.config = config;
    this.worker = worker;
    scanner = new XEndpointScanner<>(config.scanIntervalMillis, 0);
  }

  @Override
  public UndertowEndpoint connect(URI uri, XEndpointListener<? super UndertowEndpoint> listener) throws Exception {
    final int bufferSize = UndertowAtts.BUFFER_SIZE.get(config.attributes);
    final boolean directBuffers = UndertowAtts.DIRECT_BUFFERS.get(config.attributes);
    final ByteBufferPool pool = new DefaultByteBufferPool(directBuffers, bufferSize);

    final ConnectionBuilder builder = WebSocketClient.connectionBuilder(worker, pool, uri);
    if (uri.getScheme().equals("wss")) {
      final SSLContext sslContext = config.sslContextProvider.getSSLContext();
      final ByteBufferPool sslBufferPool = new DefaultByteBufferPool(directBuffers, 320 * 1024);
      final XnioSsl ssl = new UndertowXnioSsl(worker.getXnio(), OptionMap.EMPTY, sslBufferPool, sslContext);
      builder.setSsl(ssl);
    }


    final WebSocketChannel channel = builder.connect().get(); 
    return UndertowEndpoint.clientOf(scanner, channel, config, listener);
  }

  @Override
  public void close() throws Exception {
    scanner.closeEndpoints(60_000);
    scanner.close();
    worker.shutdown();
    worker.awaitTermination();
  }
  
  @Override
  public Collection<UndertowEndpoint> getEndpoints() {
    return scanner.getEndpoints();
  }
  
  @Override
  public XClientConfig getConfig() {
    return config;
  }
  
  public static final class Factory implements XClientFactory<UndertowEndpoint> {
    @Override public XClient<UndertowEndpoint> create(XClientConfig config) throws Exception {
      final int bufferSize = UndertowAtts.BUFFER_SIZE.get(config.attributes);
      return new UndertowClient(config, createXnioWorker(config), bufferSize);
    }
  }
  
  public static XClientFactory<UndertowEndpoint> factory() {
    return new Factory();
  }
  
  private static XnioWorker createXnioWorker(XClientConfig config) throws IllegalArgumentException, IOException {
    final int ioThreads = UndertowAtts.IO_THREADS.get(config.attributes);
    final int coreTaskThreads = UndertowAtts.CORE_TASK_THREADS.get(config.attributes);
    final int maxTaskThreads = UndertowAtts.MAX_TASK_THREADS.get(config.attributes);
    return Xnio.getInstance().createWorker(OptionMap.builder()
                                           .set(Options.WORKER_IO_THREADS, ioThreads)
                                           .set(Options.THREAD_DAEMON, true)
                                           .set(Options.CONNECTION_HIGH_WATER, 1_000_000)
                                           .set(Options.CONNECTION_LOW_WATER, 1_000_000)
                                           .set(Options.WORKER_TASK_CORE_THREADS, coreTaskThreads)
                                           .set(Options.WORKER_TASK_MAX_THREADS, maxTaskThreads)
                                           .set(Options.TCP_NODELAY, true)
                                           .getMap());
  }
}
