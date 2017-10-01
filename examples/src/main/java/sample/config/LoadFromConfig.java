package sample.config;

import java.net.*;

import com.obsidiandynamics.socketx.*;
import com.obsidiandynamics.socketx.undertow.*;
import com.obsidiandynamics.socketx.util.*;
import com.obsidiandynamics.yconf.*;

public final class LoadFromConfig {
  public static void main(String[] args) throws Exception {
    final XServerConfig serverConfig = new MappingContext()
        .withParser(new SnakeyamlParser())
        .fromStream(ResourceLocator.asStream(new URI("cp://sample-server-config.yaml")))
        .map(XServerConfig.class);
    
    final XServer<?> server = UndertowServer
        .factory()
        .create(serverConfig, 
                new XEndpointLambdaListener<>()
                .onConnect(System.out::println));
    
    server.close();
  }
}
