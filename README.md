Socket.x
===
[ ![Download](https://api.bintray.com/packages/obsidiandynamics/socketx/socketx-core/images/download.svg) ](https://bintray.com/obsidiandynamics/socketx/socketx-core/_latestVersion)

Socket.x is a library for building high-performance, distributed [WebSocket](https://en.wikipedia.org/wiki/WebSocket) applications. It provides a simple, consistent API for constructing both clients and servers, while giving you the flexibility to vary the underlying provider without changing your code.

# Why Socket.x
## Speed and latency
Socket.x has been benchmarked in excess of **1 million messages/second** on a 2010-era i7 quad-core CPU, using the [Undertow](http://undertow.io/) provider. [Applications](#user-content-applications-using-socketx) built on Socket.x have demonstrated message switching at **sub-millisecond latencies**.

## Asynchronous and event-driven API
Socket.x APIs are designed around asynchronous, non-blocking and event-driven interactions. This allows you service a huge number of concurrent clients while utilising a relatively few number of OS threads. Applications built on top of Socket.x have achieved in excess of 1,000,000 connections per node. To put things into context, thread-bound Web servers usually top out between one and ten thousand connections.

## Provider-independent
Socket.x isn't a WebSocket implementation _per se_. It offers a **simple, uniform API** for working with a range of WebSocket client/server implementations - called **providers**, including the industry heavy-weights [Undertow](http://undertow.io/) and [Jetty](https://www.eclipse.org/jetty). This gives you the flexibility to adopt a provider that you're most familiar with, particularly if you require certain provider-specific features.

## Simplicity
Having a standard API isn't just for provider portability. It gives you a **clean programming model** that isn't influenced by any particular provider and is generally **much easier to work with**. Everything's in one convenient place, well-documented and the same set of primitives are reused for both server and client parts of your application. By contrast, and we acknowledge the subjectivity of this statement, providers such as Jetty and Undertow, while being outstanding in many key areas, have evolved over numerous releases by a diverse group of contributors. As a result, the API is incohesive in parts, can be challenging to learn, difficult to recall and enduring to work with, and varies depending on whether you are building a server or a client.

# Getting Started
## Get the binaries
Socket.x builds are hosted on JCenter (Maven users may need to add the JCenter repository to their POM). Simply add the following snippet to your build file (replacing the version number in the snippet with the version shown on the Download badge at the top of this README).

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

The import above only gets you the Socket.x API. In addition, you'll need to import at least one provider. We recommend Undertow, purely due to its blistering performance and solid standards-compliance.
```groovy
compile 'com.obsidiandynamics.socketx:socketx-undertow:0.1.0'
```

## Basic 'echo' app
The following Java snippet demonstrates a basic client/server 'echo' app. The complete sample code is located in `examples/src/main/java`.
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

When setting up a server, or when creating a new connection, one must supply an `XEndpointListener` implementation. This is a simple interface comprising the following self-describing methods:
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

In our simple 'echo' example, we've provided `onConnect` and `onText` handlers, invoked when a connection is established and when a text message is received, respectively. The client opens a connection with `client.connect()` and sends a text message by calling `send(String)` on the newly opened endpoint. Upon receipt, the server echoes the message by calling `send(String)` on the handled endpoint. When the client receives the response, it severs the connection by calling `close()`.

The `drain()` method on an `XClient` blocks the calling thread until the client has no more live endpoints. (An equivalent method is also defined on `XServer`.) In our example this ensures that we don't prematurely clean up before the messages have gone through. (Remember, Socket.x is asynchronous - it doesn't block when sending a message.) We could've just as easily used a crude `Thread.sleep()`, but a `drain()` is much more elegant. Calling `close()` on an `XServer` or an `XClient` instance will close all underlying connections, await closure, and clean up any resources - threads, sockets, byte buffers, and so forth.


# Configuration
## Common configuration
Socket.x server and client providers are configured using the `XServerConfig` and `XClientConfig` objects respectively. These comprise attributes common to all providers, and can be assigned using the `withXxx()` chained methods. Alternatively, one can subclass the config and define these attributes in-line, using the double-brace initialization pattern, as shown in the snippet below. Beware though, as the usual caveats apply; this technique inadvertently results in an anonymous inner class, holding a reference to its enclosing object.
```java
new XServerConfig() {{
  path = "/echo";
  port = 8080;
}};
```

### High-water mark
A high-water mark (HWM) acts as a hard cut-off point for the number of outstanding (queued, but not yet sent) messages on a given endpoint connection. When a connection reaches its HWM (typically through repeated unthrottled calls to `send()`), the new messages will be discarded. By default, the HWM is set to `Long.MAX_VALUE`, which effectively means no HWM.

An HWM can apply to both server-side and client-side endpoints. To set a HWM, follow the snippet below:
```java
// for a server
new XServerConfig().withHighWaterMark(1000);
// or, in the case of a client
new XClientConfig().withHighWaterMark(1000);
```

HWMs are discussed in more detail in the [flow control](#user-content-flow-control) section.

### Connection keep-alive
Often, in WebSocket applications, we need to know if the counter-party is still there. This isn't always obvious, particularly if the connection carries spurious traffic and may be idle for extended periods of time. The idle state poses another challenge - the TCP stack of either party may forcibly close the connection after a period of inactivity.

The WebSocket protocol supports keep-alives using a pair of [Ping and Pong frames](https://tools.ietf.org/html/rfc6455#section-5.5.2). By convention, it is the responsibility of the server to send a ping frame, which should be duly reciprocated with a pong frame. 

While  giving you manual control over the ability over sending ping frames should you require it, Socket.x also offers a **scanner** that monitors connection activity and automatically sends pings during periods of inactivity. In addition, the scanner can forcibly terminate a connection that has been idle for too long (which also implies that it hasn't responded to a ping). Both the server and the client are equipped with scanners; however, only the server's scanner will initiate a ping. Both scanners are capable of terminating idle connections.

The following snippet configures the scanner's maximum keep-alive interval and idle timeouts (all times are in milliseconds):
```java
// on the server
new XServerConfig().withPingInterval(300_000).withIdleTimeout(600_000);
// on the client
new XServerConfig().withIdleTimeout(600_000);
```

**Note**: Since WebSockets are backed by TCP, the latter has a low-level mechanism for keeping connections alive (the `SO_KEEPALIVE` option in *NIX and Windows), which is entirely separate to the WebSockets' own ping/pong frames. Although you might have control over your runtime environment, and be tempted to use TCP keep-alives, consider that you typically have little to no control over the intermediate networking infrastructure, particularly if your application communicates over the public Internet. Certain network elements, such as proxies, which are typically optimised for short-lived HTTP connections, may prematurely terminate your long-lived WebSocket connection due to inactivity. As such, it's strongly recommended that you always use the WebSocket keep-alive mechanism independently of what the underlying TCP stack is configured for. (Unless, of course, if the TCP stack uses a more aggressive setting than your WebSocket keep-alives, in which case one or the other needs to change.) 

### SSL

## Provider-specific configuration
//TODO

## Loading with YConf
//TODO


# Advanced topics
## Send callback

## Flow control
Without a HWM set, a connection may buffer messages indefinitely if the producer is sending at a rate higher than the consumer can receive.

## Servlet support

## Connection termination

## Utilities
Socket.x comes with a couple of utility classes that can come in very handy when working with not only WebSockets, but other sockets and protocols in general.

### `Asserter`

### `Await`

### `BinaryUtils`

### `ResourceLocator`

### `SocketUtils`

### `URIBuilder`


# Applications using Socket.x
The following is a list of applications using Socket.x that we're _aware_ of:

* [Flywheel](https://github.com/william-hill-community/flywheel) - a high-performance, distributed IoT message broker.

