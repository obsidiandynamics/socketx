package com.obsidiandynamics.socketx;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import com.obsidiandynamics.indigo.util.*;

final class DefaultServerHarness extends ServerHarness implements TestSupport {
  private final AtomicBoolean ping = new AtomicBoolean(true);
  private final XServer<XEndpoint> server;
  private final XSendCallback writeCallback;
  
  DefaultServerHarness(XServerConfig config, XServerFactory<XEndpoint> factory, ServerProgress progress) throws Exception {
    final XEndpointListener<XEndpoint> serverListener = new XEndpointListener<XEndpoint>() {
      @Override public void onConnect(XEndpoint endpoint) {
        log("s: connected %s\n", endpoint.getRemoteAddress());
        connected.incrementAndGet();
        keepAlive(endpoint, ping, config.idleTimeoutMillis);
      }

      @Override public void onText(XEndpoint endpoint, String message) {
        log("s: received: %s\n", message);
        received.incrementAndGet();
      }

      @Override public void onBinary(XEndpoint endpoint, ByteBuffer message) {
        log("s: received %d bytes\n", message.limit());
        received.incrementAndGet();
      }
      
      @Override public void onDisconnect(XEndpoint endpoint, int statusCode, String reason) {
        log("s: disconnected: statusCode=%d, reason=%s\n", statusCode, reason);
      }
      
      @Override public void onError(XEndpoint endpoint, Throwable cause) {
        log("s: socket error\n");
        System.err.println("s: server socket error");
        cause.printStackTrace();
      }

      @Override public void onClose(XEndpoint endpoint) {
        log("s: closed\n");
        closed.incrementAndGet();
        ping.set(false);
      }

      @Override public void onPing(XEndpoint endpoint, ByteBuffer data) {
        log("s: ping\n");
      }

      @Override public void onPong(XEndpoint endpoint, ByteBuffer data) {
        log("s: pong\n");
      }
    };
    
    writeCallback = new XSendCallback() {
      @Override public void onComplete(XEndpoint endpoint) {
        final long s = sent.getAndIncrement();
        if (s % 1000 == 0) progress.update(DefaultServerHarness.this, s);
      }

      @Override public void onError(XEndpoint endpoint, Throwable cause) {
        System.err.println("s: server write error");
        cause.printStackTrace();
      }

      @Override public void onSkip(XEndpoint endpoint) {
        log("s: skipped sending\n");
      }
    };
    server = factory.create(config, serverListener);
  }

  @Override
  public void close() throws Exception {
    server.close();
  }

  @Override
  public List<XEndpoint> getEndpoints() {
    return new ArrayList<>(server.getEndpointManager().getEndpoints());
  }

  @Override
  public void broadcast(List<XEndpoint> endpoints, byte[] payload) {
    for (XEndpoint endpoint : endpoints) {
      endpoint.send(ByteBuffer.wrap(payload), writeCallback);
    }
  }

  @Override
  public void broadcast(List<XEndpoint> endpoints, String payload) {
    for (XEndpoint endpoint : endpoints) {
      endpoint.send(payload, writeCallback);
    }
  }

  @Override
  public void flush(List<XEndpoint> endpoints) throws IOException {
    for (XEndpoint endpoint : endpoints) {
      endpoint.flush();
    }
  }

  @Override
  public void sendPing(XEndpoint endpoint) {
    endpoint.sendPing();
  }
}
