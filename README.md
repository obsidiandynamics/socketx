Socket.x
===
[ ![Download](https://api.bintray.com/packages/obsidiandynamics/socketx/socketx-core/images/download.svg) ](https://bintray.com/obsidiandynamics/socketx/socketx-core/_latestVersion)

Socket.x is a library for building high-performance [WebSocket](https://en.wikipedia.org/wiki/WebSocket) applications. It provides a simple, consistent API for constructing both clients and servers, while giving you the flexibility to vary the underlying provider.

# Why Socket.x
## Speed and latency
Socket.x has been benchmarked in excess of **1 million messages/second** on a 2010-era i7 quad-core CPU, using the [Undertow](http://undertow.io/) provider. [Applications](#user-content-applications-using-socketx) built on Socket.x have demonstrated message switching at **sub-millisecond latencies**.

## Asynchronous and event-driven API
Socket.x APIs are designed around asynchronous, non-blocking and event-driven interactions. This allows you service a huge number of concurrent clients while utilising a relatively few number of OS threads. Applications built on top of Socket.x have achieved in excess of 1,000,000 connections per node. To put things into context, thread-bound Web servers usually top out between one and ten thousand connections.

## Provider-independent
Socket.x isn't a WebSocket implementation _per se_. It offers a **uniform API** for working with a range of WebSocket client/server implementations - called **providers**, including the industry heavy-weights [Undertow](http://undertow.io/) and [Jetty](https://www.eclipse.org/jetty). This gives you the flexibility to adopt a provider that you're most familiar with, particularly if you require certain provider-specific features.

# Getting Started
## Get the binaries
Socket.x builds are hosted on JCenter (Maven users might have to add the JCenter repository to their POM). Simply add the following snippet to your build file (replacing the version number in the snippet with the version shown on the Download badge at the top of this README).

For Maven:

```xml
<dependency>
  <groupId>com.obsidiandynamics.socketx</groupId>
  <artifactId>socketx-core</artifactId>
  <version>0.1.0</version>
  <type>pom</type>
</dependency>
```

For Gradle:

```groovy
compile 'com.obsidiandynamics.socketx:socketx-core:0.1.0'
```

The import above only gets you the Socket.x API. In addition, you'll need to import at least one provider. We recommend Undertow, purely based on its performance and standards-compliance.
```groovy
compile 'com.obsidiandynamics.socketx:socketx-undertow:0.1.0'
```

## Write the code
The following Java snippet demonstrates a basic client/server "echo" app. The complete sample code is located in `examples/src/main/java`.
```java
XServer<?> server = UndertowServer
    .factory()
    .create(new XServerConfig()
            .withPath("/echo")
            .withPort(8080), 
            new XEndpointLambdaListener<>()
            .onConnect(e -> { 
              System.out.format("Server: connected %s\n", e); 
            })
            .onText((e, message) -> {
              System.out.format("Server: received '%s'\n", message);
              e.send("Hello reply from server");
            }));

XClient<?> client = UndertowClient
    .factory()
    .create(new XClientConfig());

XEndpoint clientEndpoint = client
    .connect(new URI("ws://localhost:8080/echo"),
             new XEndpointLambdaListener<>()
             .onConnect(e -> { 
               System.out.format("Client: connected %s\n", e); 
             })
             .onText((e, message) -> {
               System.out.format("Client: received '%s'\n", message);
               try {
                e.close();
              } catch (Exception ex) {
                ex.printStackTrace();
              }
             }));

clientEndpoint.send("Hello from client");

client.drain();
client.close();
server.close();
```

The two main players here are `XServer<? extends XEndpoint>` and `XClient<? extends XEndpoint>`. `XServer` accepts connections, while `XClient` lets you create connections. Both must be instantiated by way of a factory - in our case we're using `UndertowServer.factory()` and `UndertowClient.factory()`. We could've easily used `JettyServer`/`JettyClient` instead, without changing any other code, providing we first import `socketx-jetty` in our `build.gradle`. We could've even mixed `JettyServer` with `UndertowClient`.

The next item of significance is `XServerConfig` and `XClientConfig` classes - a single, uniform mechanism for configuring Socket.x, irrespective of the provider. (It can also be used to pass [provider-specific configuration](#user-content-provider-specific-configuration)). In our example, we're asking the server to listen on port `8080` and serve content on the `/echo` path. We're also using the default `XClientConfig`.

When setting up a server, or when creating a new connection, one must supply an `XEndpointListener<? extends XEndpoint>` implementation.



# Provider-Specific Configuration
//TODO

# Applications using Socket.x
* [Flywheel](https://github.com/william-hill-community/flywheel) - a high-performance, distributed IoT message broker.

