package com.obsidiandynamics.socketx;

import java.nio.*;

import com.obsidiandynamics.indigo.util.*;
import com.obsidiandynamics.socketx.util.*;
import com.obsidiandynamics.socketx.util.URIBuilder.*;

public final class DefaultClientHarness extends ClientHarness implements TestSupport {
  private final XSendCallback writeCallback;
  
  private final XEndpoint endpoint;
  
  DefaultClientHarness(XClient<?> client, Ports ports, boolean https, boolean echo) throws Exception {
    final XEndpointListener<XEndpoint> clientListener = new XEndpointListener<XEndpoint>() {
      @Override public void onConnect(XEndpoint endpoint) {
        log("c: connected: %s\n", endpoint.getRemoteAddress());
        connected.set(true);
      }

      @Override public void onText(XEndpoint endpoint, String message) {
        log("c: received: %s\n", message);
        received.incrementAndGet();
        if (echo) {
          send(ByteBuffer.wrap(message.getBytes()));
        }
      }

      @Override public void onBinary(XEndpoint endpoint, ByteBuffer message) {
        log("c: received\n");
        received.incrementAndGet();
        if (echo) {
          send(message);
        }
      }
      
      @Override public void onDisconnect(XEndpoint endpoint, int statusCode, String reason) {
        log("c: disconnected: statusCode=%d, reason=%s\n", statusCode, reason);
      }
      
      @Override public void onError(XEndpoint endpoint, Throwable cause) {
        log("c: socket error\n");
        System.err.println("c: client socket error");
        cause.printStackTrace();
      }

      @Override public void onClose(XEndpoint endpoint) {
        log("c: closed\n");
        closed.set(true);
      }

      @Override public void onPing(XEndpoint endpoint, ByteBuffer data) {
        log("c: ping\n");
      }

      @Override public void onPong(XEndpoint endpoint, ByteBuffer data) {
        log("c: pong\n");
      }
    };
    
    endpoint = client.connect(URIBuilder
                              .create()
                              .withWebSocket(true)
                              .withHttps(https)
                              .withHost("localhost")
                              .withPortProvider(ports)
                              .withPath("/")
                              .build(), 
                              clientListener);
    
    writeCallback = new XSendCallback() {
      @Override public void onComplete(XEndpoint endpoint) {
        sent.incrementAndGet();
      }

      @Override public void onError(XEndpoint endpoint, Throwable throwable) {
        System.err.println("c: client write error");
        throwable.printStackTrace();
      }

      @Override public void onSkip(XEndpoint endpoint) {
        log("c: skipped sending\n");
      }
    };
  }
  
  private void send(ByteBuffer payload) {
    endpoint.send(payload, writeCallback);
  }
  
  @Override
  public void close() throws Exception {
    endpoint.close();
  }
}
