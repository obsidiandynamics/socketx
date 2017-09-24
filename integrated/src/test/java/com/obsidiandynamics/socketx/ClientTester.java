package com.obsidiandynamics.socketx;

import java.net.*;
import java.nio.*;

import com.obsidiandynamics.indigo.util.*;
import com.obsidiandynamics.socketx.undertow.*;

public final class ClientTester {
  public static void main(String[] args) throws URISyntaxException, Exception {
    final XClientConfig config = new XClientConfig() {{
      
    }};
    final XClient<?> client = UndertowClient.factory().create(config);
    final XEndpoint endpoint = client.connect(new URI("wss://echo.websocket.org"), new XEndpointListener<XEndpoint>() {
      @Override
      public void onConnect(XEndpoint endpoint) {
        System.out.format("onConnect: %s\n", endpoint);
      }

      @Override
      public void onText(XEndpoint endpoint, String message) {
        System.out.format("onText: %s, message: %s\n", endpoint, message);
      }

      @Override
      public void onBinary(XEndpoint endpoint, ByteBuffer message) {
        System.out.format("onBinary: %s, message.remaining: %d\n", endpoint, message.remaining());
      }

      @Override
      public void onPing(XEndpoint endpoint, ByteBuffer data) {
        System.out.format("onPing: %s\n", endpoint);
      }

      @Override
      public void onPong(XEndpoint endpoint, ByteBuffer data) {
        System.out.format("onPong: %s\n", endpoint);
      }

      @Override
      public void onDisconnect(XEndpoint endpoint, int statusCode, String reason) {
        System.out.format("onDisconnect: %s, statusCode: %d, reason: %s\n", endpoint, statusCode, reason);
      }

      @Override
      public void onClose(XEndpoint endpoint) {
        System.out.format("onClose: %s\n", endpoint);
      }

      @Override
      public void onError(XEndpoint endpoint, Throwable cause) {
        System.out.format("onError: %s, cause: %s\n", endpoint, cause);
        cause.printStackTrace();
      }
    });
    
    for (int i = 0; i < 10; i++) {
      endpoint.send("hello world", null);
      TestSupport.sleep(500);
    }
  }
}
