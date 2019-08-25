package sample.ssl;

import java.net.*;

import com.obsidiandynamics.socketx.*;
import com.obsidiandynamics.socketx.ssl.*;
import com.obsidiandynamics.socketx.undertow.*;

public final class WSS {
  private static final String TRUST_STORE_PASSWORD = "storepass";
  private static final String KEY_STORE_PASSWORD = "keypass";
  private static final String STORE_LOCATION = "cp://keystore-dev.jks";
  
  public static void main(String[] args) throws Exception {
    final XServer<?> server = UndertowServer
        .factory()
        .create(new XServerConfig()
                .withPath("/echo")
                .withPort(8080)
                .withHttpsPort(8443)
                .withSSLContextProvider(new CompositeSSLContextProvider()
                                        .withKeyManagerProvider(new JKSKeyManagerProvider()
                                                                .withLocation(STORE_LOCATION)
                                                                .withStorePassword(TRUST_STORE_PASSWORD)
                                                                .withKeyPassword(KEY_STORE_PASSWORD))
                                        .withTrustManagerProvider(new JKSTrustManagerProvider()
                                                                  .withLocation(STORE_LOCATION)
                                                                  .withStorePassword(TRUST_STORE_PASSWORD))), 
                new XEndpointLambdaListener<>()
                .onConnect(System.out::println));

    final XClient<?> client = UndertowClient
        .factory()
        .create(new XClientConfig()
                .withSSLContextProvider(new CompositeSSLContextProvider()
                                        .withTrustManagerProvider(new JKSTrustManagerProvider()
                                                                  .withLocation(STORE_LOCATION)
                                                                  .withStorePassword(TRUST_STORE_PASSWORD))));

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
