package sample.echo;

import java.net.*;

import com.obsidiandynamics.socketx.*;
import com.obsidiandynamics.socketx.undertow.*;

public final class Echo {
  public static void main(String[] args) throws Exception {
    final XServer<?> server = UndertowServer
        .factory()
        .create(new XServerConfig()
                .withPath("/echo")
                .withPort(8080), 
                new XEndpointLambdaListener<>()
                .onConnect(endpoint -> { 
                  System.out.format("Server: connected %s\n", endpoint); 
                })
                .onText((endpoint, message) -> {
                  System.out.format("Server: received '%s'\n", message);
                  endpoint.send("Hello reply from server");
                }));

    final XClient<?> client = UndertowClient
        .factory()
        .create(new XClientConfig());

    final XEndpoint clientEndpoint = client
        .connect(new URI("ws://localhost:8080/echo"),
                 new XEndpointLambdaListener<>()
                 .onConnect(endpoint -> { 
                   System.out.format("Client: connected %s\n", endpoint); 
                 })
                 .onText((endpoint, message) -> {
                   System.out.format("Client: received '%s'\n", message);
                   try {
                     endpoint.close();
                   } catch (Exception e) {
                     e.printStackTrace();
                   }
                 }));
    
    clientEndpoint.send("Hello from client");
    
    client.drain();
    client.close();
    server.close();
  }
}
