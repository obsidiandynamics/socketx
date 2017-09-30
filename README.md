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
            .onConnect(endpoint -> { 
              System.out.format("Server: connected %s\n", endpoint); 
            })
            .onText((endpoint, message) -> {
              System.out.format("Server: received '%s'\n", message);
              endpoint.send("Hello reply from server");
            }));

XClient<?> client = UndertowClient
    .factory()
    .create(new XClientConfig());

XEndpoint clientEndpoint = client
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
```

Run the example above. The output should resemble the following:
```
Client: connected UndertowEndpoint [remote=localhost/127.0.0.1:8080, lastActivity=2017-09-30T22:21:10.241+10:00[Australia/Sydney]]
Server: connected UndertowEndpoint [remote=/127.0.0.1:57561, lastActivity=2017-09-30T22:21:10.241+10:00[Australia/Sydney]]
Server: received 'Hello from client'
Client: received 'Hello reply from server'
```

The two key players in Socket.x are `XServer` and `XClient`. `XServer` accepts connections, while `XClient` lets you create connections. Both must be instantiated through an `XServerFactory` and an `XClientFactory`; a default pair of factories is available for each supported provider. In our example, we've chosen to use `UndertowServer.factory()` and `UndertowClient.factory()`. We could've just as easily used `JettyServer`/`JettyClient` instead, without changing any other code, providing we first import `socketx-jetty` in our `build.gradle`. You can even mix `JettyServer` with `UndertowClient`.

The next item of significance is `XServerConfig` and `XClientConfig` classes - a single, uniform mechanism for [configuring Socket.x](#user-content-configuration), irrespective of the provider. (It can also be used to pass [provider-specific configuration](#user-content-provider-specific-configuration)). In our example, we're asking the server to listen on port `8080` and publish endpoints on the `/echo` path. We're also sticking with the default `XClientConfig` in this case.

When setting up a server, or when creating a new connection, one must supply an `XEndpointListener` implementation. This is a simple interfacing comprising the following self-describing methods:
```java
void onConnect(E endpoint);
void onText(E endpoint, String message);
void onBinary(E endpoint, ByteBuffer message);
void onPing(E endpoint, ByteBuffer data);
void onPong(E endpoint, ByteBuffer data);
void onDisconnect(E endpoint, int statusCode, String reason);
void onClose(E endpoint);
void onError(E endpoint, Throwable cause);
```

If you favour functional programming, or need to selectively handle certain events (while ignoring others), use the `XEndpointLambdaListener`. Simply provide a lambda for each of the `onXxx` methods, using the same signature as the corresponding method in the `XEndpointListener` interface. Alternatively, you can just subclass `XEndpointLambdaListener` directly, overriding the methods you see fit.

In our simple "echo" example, we've provided `onConnect` and `onText` handlers, invoked when a connection is established and when a text message is received, respectively. The client opens a connection with `client.connect()` and sends a text message by calling `send(String)` on the newly opened endpoint. Upon receipt, the server echoes the message by calling `send(String)` on the handled endpoint. When the client receives the response, it severs the connection by calling `close()`.

The `drain()` method on an `XClient` blocks the calling thread until the client has no more live endpoints. (An equivalent method is also defined on `XServer`.) In our example this ensures that we don't prematurely clean up before the messages have gone through. (Remember, Socket.x is asynchronous - it doesn't block when sending a message.) We could've just as easily used a crude `Thread.sleep()`, but a `drain()` is much more elegant. Calling `close()` on an `XServer` or an `XClient` instance will close all underlying connections, await closure, and clean up any resources - threads, sockets, byte buffers, and so forth.


# Configuration
## Common configuration
Socket.x server and client providers are configured using the `XServerConfig` and `XClientConfig` objects respectively. These comprise attributes common to all providers, and can be assigned using the `withXxx()` chained methods. Alternatively, one can subclass the config and define these attributes in-line, using the double-brace initialization pattern, as shown in the snippet below. Beware though, as the usual caveats apply; this technique inadvertently results in an anonymous inner class, which holds a reference to its enclosing object.
```java
new XServerConfig() {{
  path = "/echo";
  port = 8080;
}};
```

### High-water mark

### Connection keep-alive

### SSL

## Provider-specific configuration
//TODO

## Loading with YConf
//TODO

# Advanced topics
## Send callback

## Servlet support

## Connection termination

## Utilities
Socket.x comes with a couple of utility classes that can come in very handy when working with not only WebSockets, but sockets and protocols in general.

### `Asserter`

### `Await`

### `BinaryUtils`

### `ResourceLocator`

### `SocketUtils`

### `URIBuilder`

# Applications using Socket.x
* [Flywheel](https://github.com/william-hill-community/flywheel) - a high-performance, distributed IoT message broker.

