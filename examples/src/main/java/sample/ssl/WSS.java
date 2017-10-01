package sample.ssl;

import java.net.*;

import com.obsidiandynamics.socketx.*;
import com.obsidiandynamics.socketx.ssl.*;
import com.obsidiandynamics.socketx.undertow.*;

public final class WSS {
  public static void main(String[] args) throws Exception {
    final XServer<?> server = UndertowServer
        .factory()
        .create(new XServerConfig()
                .withPath("/echo")
                .withPort(8080)
                .withHttpsPort(8443)
                .withSSLContextProvider(new CompositeSSLContextProvider()
                                        .withKeyManagerProvider(new JKSKeyManagerProvider()
                                                                .withLocation("cp://keystore-dev.jks")
                                                                .withStorePassword("storepass")
                                                                .withKeyPassword("keypass"))
                                        .withTrustManagerProvider(new JKSTrustManagerProvider()
                                                                  .withLocation("cp://keystore-dev.jks")
                                                                  .withStorePassword("storepass"))), 
                new XEndpointLambdaListener<>()
                .onConnect(System.out::println));

    final XClient<?> client = UndertowClient
        .factory()
        .create(new XClientConfig()
                .withSSLContextProvider(new CompositeSSLContextProvider()
                                        .withTrustManagerProvider(new JKSTrustManagerProvider()
                                                                  .withLocation("cp://keystore-dev.jks")
                                                                  .withStorePassword("storepass"))));

    final XEndpoint clientEndpoint = client
        .connect(new URI("wss://localhost:8443/echo"),
                 new XEndpointLambdaListener<>()
                 .onConnect(System.out::println));

    clientEndpoint.close();
    clientEndpoint.awaitClose(1000);

    client.close();
    server.close();
  }
}
