Socket.x
===
[ ![Download](https://api.bintray.com/packages/obsidiandynamics/socketx/socketx-core/images/download.svg) ](https://bintray.com/obsidiandynamics/socketx/socketx-core/_latestVersion)

Socket.x is a library for building high-performance, distributed [WebSocket](https://en.wikipedia.org/wiki/WebSocket) applications. It provides a simple, consistent API for constructing both clients and servers, while giving you the flexibility to vary the underlying provider without changing your code.

# Why Socket.x
## Speed and latency
Socket.x has been benchmarked in excess of **1 million messages/second** on a 2010-era i7 quad-core CPU, using the [Undertow](http://undertow.io/) provider. [Applications built on Socket.x](#user-content-applications-using-socketx) have demonstrated message switching at **sub-millisecond latencies**.

## Asynchronous and event-driven API
Socket.x APIs are designed around asynchronous, non-blocking and event-driven interactions. This allows you service a huge number of concurrent clients while utilising a relatively few number of OS threads. Applications built with Socket.x have achieved in excess of 1,000,000 connections per node. To put things into context, thread-bound Web servers usually top out between one and ten thousand connections.

## Provider-independent
Socket.x isn't a WebSocket implementation _per se_. It offers a **simple, uniform API** for working with a range of compliant WebSocket client/server implementations, called **providers**, including the industry heavy-weights [Undertow](http://undertow.io/) and [Jetty](https://www.eclipse.org/jetty). This gives you the flexibility to adopt a provider that you're most familiar with, particularly if you require certain provider-specific features.

## Simplicity
Having a standard API isn't just for provider portability. It gives you a **clean programming model** that isn't influenced by any particular provider and is generally **much easier to work with**. Everything's in one convenient place, well-documented and the same set of primitives are reused for both server and client parts of your application. By contrast, and we acknowledge the subjectivity of this statement, providers such as Jetty and Undertow, while being outstanding in many key areas, have evolved over numerous generational  with help from a diverse group of contributors. They also typically accommodate multiple use cases - not just WebSockets, but the full breadth of the HTTP spectrum. As a result, the API is incohesive in parts, can be challenging to learn, difficult to recall and enduring to work with, and varies depending on whether you are building a server or a client. Even seemingly simple things, such as configuring SSL, requires a significant amount of reading up on. By contrast, Socket.x is designed ground-up specifically for WebSockets and asynchronous I/O, presenting a pure and uncompromised coding experience.

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

The import above only gets you the Socket.x core API. In addition, you'll need to import at least one provider. We recommend Undertow, purely due to its blistering performance and solid standards-compliance.
```groovy
compile 'com.obsidiandynamics.socketx:socketx-undertow:0.1.0'
```

## Basic 'echo' app
The following Java snippet demonstrates a basic client/server 'echo' app - sending a message from the client to the server and receiving a response. The complete sample code is located in `examples/src/main/java/sample/echo`.
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

The two key players in Socket.x are `XServer` and `XClient`. `XServer` accepts connections, while `XClient` lets you create outgoing connections. They must be instantiated through an `XServerFactory` or an `XClientFactory`, as appropriate; a default pair of factories is available for each supported provider. In our example, we've chosen to use `UndertowServer.factory()` and `UndertowClient.factory()`. We could've just as easily used `JettyServer`/`JettyClient` instead, without changing any other code, providing we first import `socketx-jetty` in our `build.gradle`. You can even mix `JettyServer` with `UndertowClient`; the client and server subsystems are completely independent of one another.

The next item of significance is `XServerConfig` and `XClientConfig` - a single, uniform mechanism for [configuring Socket.x](#user-content-configuration), irrespective of the provider. (It can also be used to pass [provider-specific configuration](#user-content-provider-specific-configuration)). In our example, we're asking the server to listen on port `8080` and publish an endpoint on the `/echo` path. In this example, we're sticking with the default `XClientConfig` for our client.

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

If you favour functional programming, or need to selectively handle certain events (while ignoring others), use the `XEndpointLambdaListener`. Simply provide a lambda for each of the `onXxx` methods, using the same signature as the corresponding method in the `XEndpointListener` interface, as was illustrated in our earlier example. Alternatively, you can just subclass `XEndpointLambdaListener` directly, overriding the methods you see fit.

In our simple 'echo' example, we've provided `onConnect` and `onText` handlers, invoked when a connection is established and when a text message is received, respectively. The client opens a connection with `client.connect()` and sends a text message by calling `send(String)` on the newly opened endpoint. Upon receipt, the server echoes the message by calling `send(String)` on the handled endpoint. When the client receives the response, it severs the connection by calling `close()`.

The `drain()` method on an `XClient` blocks the calling thread until the client container has no more live endpoints. (An equivalent method is also defined on `XServer`.) In our example this ensures that we don't prematurely clean up before the messages have gone through. (Remember, Socket.x is asynchronous - it doesn't block when sending a message.) We could've just as easily used a crude `Thread.sleep()`, but a `drain()` is much more elegant and efficient. 

Calling `close()` on an `XServer` or an `XClient` instance will attempt to close all underlying connections, await closure, and clean up any resources - threads, sockets, byte buffers, and so forth.


# Configuration
## Common configuration
Socket.x server and client providers are configured using the `XServerConfig` and `XClientConfig` objects respectively. These comprise attributes common to all providers, and can be assigned using the `withXxx()` chained methods. Alternatively, one can subclass the config and define these attributes in-line, using the double-brace initialization pattern, as shown in the snippet below. Beware though, as the usual caveats apply; this technique inadvertently results in an anonymous inner class, holding a reference to its enclosing object. (We're _not_ advocating this pattern for the aforementioned reasons; we're merely stating that it's use is viable.)
```java
new XServerConfig() {{
  path = "/echo";
  port = 8080;
}};
```

### High-water mark
A high-water mark (HWM) acts as a hard cut-off point for the number of outstanding (queued, but not yet sent) messages on a given endpoint connection. When a connection reaches its HWM (typically through repeated unthrottled calls to `send()`), any new messages that would otherwise land above the high-water mark will be automatically discarded. By default, the HWM is set to `Long.MAX_VALUE`, which effectively means no HWM. It needs to be enabled explicitly.

A HWM can apply to both server-side and client-side endpoints. To set a HWM, follow the snippet below:
```java
// for a server
new XServerConfig().withHighWaterMark(1000);
// or, in the case of a client
new XClientConfig().withHighWaterMark(1000);
```

HWMs are discussed in more detail in the context of [flow control](#user-content-flow-control).

### Connection keep-alive
Often, in WebSocket applications, we need to know if the counter-party is still there. This isn't always obvious, particularly if the connection carries spurious traffic and may be idle for extended periods of time. The idle state poses another challenge - the TCP stack of either party, or an intermediary, may forcibly close the connection after a period of inactivity.

The WebSocket protocol supports layer 7 keep-alives using a pair of [Ping and Pong frames](https://tools.ietf.org/html/rfc6455#section-5.5.2). By convention, it is the responsibility of the server to send a Ping frame, which should be duly reciprocated with a Pong frame from the client. 

Socket.x gives you manual control over sending Ping frames, should you require it. However, Socket.x also offers a **scanner** that monitors connection activity and automatically sends Pings during periods of inactivity. In addition, an idle timeout setting can be optionally assigned to terminate a connection that has been inactive for a set period of time. Only the server-side scanner will send Ping frames; however, both the server and the client containers can be configured to terminate connections due to inactivity. (The actual timeout detection and connection termination fall within the responsibilities of the underlying provider.)

The following snippet configures the server's maximum keep-alive interval and sets idle timeouts (all times are in milliseconds):
```java
// on the server
new XServerConfig().withPingInterval(300_000).withIdleTimeout(600_000);
// on the client
new XServerConfig().withIdleTimeout(600_000);
```

**Note**: Since WebSockets are backed by TCP, the latter has a low-level mechanism for keeping connections alive (the `SO_KEEPALIVE` option in *NIX and Windows), which is entirely separate to the WebSockets' own Ping/Pong frames. Although you might have control over your runtime environment, and may be tempted to use TCP keep-alives, consider that you typically have little to no control over the intermediate networking infrastructure, particularly if your application communicates over the public Internet. Certain network elements, such as proxies, which are typically optimised for short-lived HTTP connections, may prematurely terminate your long-lived WebSocket connection due to inactivity. As such, it's strongly recommended that you always use the WebSocket keep-alive mechanism independently of what the underlying TCP stack is configured for. (Unless, of course, if the TCP stack uses a more aggressive setting than your WebSocket keep-alives, in which case one or the other needs to change.) 

### SSL
Enabling SSL (to get the `wss://` protocol, which is just HTTPS behind the scenes) requires two things - selecting a HTTPS port and assigning an `SSLContext`. The following snippet demonstrates a simple, albeit somewhat naive SSL setup using self-signed certificates that is suitable for a development environment. The complete code listing is available at `examples/src/main/java/sample/ssl`.
```java
XServer<?> server = UndertowServer
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

XClient<?> client = UndertowClient
    .factory()
    .create(new XClientConfig()
            .withSSLContextProvider(new CompositeSSLContextProvider()
                                    .withTrustManagerProvider(new JKSTrustManagerProvider()
                                                              .withLocation("cp://keystore-dev.jks")
                                                              .withStorePassword("storepass"))));

XEndpoint clientEndpoint = client
    .connect(new URI("wss://localhost:8443/echo"),
             new XEndpointLambdaListener<>()
             .onConnect(System.out::println));
```

Dissecting the above (which is minor a rehash of the 'echo' example) we see two changes: the call to `withHttpsPort(int)` on the `XServerConfig` instance, as well as `withSSLContextProvider(SSLContextProvider)` on both `XServerConfig` and `XClientConfig`.

The `SSLContextProvider` interface is a factory for supplying an instance of `javax.net.ssl.SSLContext` - the standard way of configuring SSL in Java and [Java Secure Socket Extension](http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.html) (JSSE). Socket.x comes with two out-of-the-box implementations.

#### `DefaultSSLContextProvider`
The equivalent of `SSLContext.getDefault()`. The resulting SSL context uses the default settings, and can be configured with the standard JSSE system properties, such as `javax.net.ssl.keyStore`, `javax.net.ssl.trustStore`, `javax.net.ssl.keyStorePassword`, etc. For more information, please consult the [JSSE reference guide](http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.html).

#### `CompositeSSLContextProvider` 
Offers a simple way of compositing factories of `javax.net.ssl.KeyManager` and `javax.net.ssl.TrustManager` instances, using `KeyManagerProvider` and `TrustManagerProvider` interfaces. The advantage of using a `CompositeSSLContextProvider` over a `DefaultSSLContextProvider` is that the former lets configure JSSE on a case-by-case basis, rather than specifying a single 'global' configuration that's applied throughout your application. So if your application uses SSL in several places, each requiring its own separate JSSE configuration, you'll need to either use a `CompositeSSLContextProvider`, or roll your own `SSLContextProvider` implementation.

Our earlier example uses a `CompositeSSLContextProvider` to load a JKS `KeyStore` from a self-signed key store that ships with Socket.x, named `keystore-dev.jks`. The built-in key store houses both a private key and a corresponding X.509 certificate, but only the certificate is specified on the client, since we're using server authentication. The `JKSKeyManagerProvider` and `JKSTrustManagerProvider` do as the name suggests, loading a key/trust store from a given location. The location is specified as a URI in the form `cp://...` for loading resources from the classpath, or `file://...` for reading from the local file system. As a convenience, `CompositeSSLContextProvider` has an additional static method - `getDevServerDefault()`, which loads the default key/trust store, as per the above example, but without the added verbosity. So the block
```java
.withSSLContextProvider(new CompositeSSLContextProvider()
                        .withKeyManagerProvider(new JKSKeyManagerProvider()
                                                .withLocation("cp://keystore-dev.jks")
                                                .withStorePassword("storepass")
                                                .withKeyPassword("keypass"))
                        .withTrustManagerProvider(new JKSTrustManagerProvider()
                                                  .withLocation("cp://keystore-dev.jks")
                                                  .withStorePassword("storepass")))
```
becomes simply
```java
.withSSLContextProvider(new CompositeSSLContextProvider().getDevServerDefault())
```

You can also use `getDevServerDefault()` on the client side, which ends up trusting the embedded self-signed certificate, and _only_ that certificate. Again, this is equivalent to our earlier example.

Sometimes, especially while developing, we may want to trust _all_ server certificates, irrespective of which party signed them or whether or not they're in our trust store. In other words, we want to use WSS purely for consistency, without concerning ourselves with certificate signing and distribution at this stage. This is accommodated by the `LenientX509TrustManagerProvider` class, which you can use in place of a `JKSTrustManagerProvider`.

**Note:** At present, Socket.x only supports server authentication, as this is the dominant use case. Client authentication may be added in later versions.

**Note:** SSL configuration applies to _both_ WebSocket and conventional HTTP traffic. In other words, the same `SSLContext` can be used to encrypt not only WSS, but also HTTPS endpoints. This is useful when publishing [Servlets](#user-content-servlets).

## Provider-specific configuration
Among the chief challenges of developing a provider-neutral WebSocket API is dealing with the edge cases, where one provider may offer something over another that isn't necessarily prescribed in the [WebSocket RFC](https://tools.ietf.org/html/rfc6455), or isn't a mandatory aspect of the protocol. An example could be the use of frame compression, multiplexing/channels or some other extension of the protocol. Alternatively, it may be some non-functional aspect of the implementation, such as the number of threads used for I/O, whether direct buffers should be used, or the underlying TCP socket (`SO_xxx`) options. There simply isn't a way of using a common set of configuration objects and a fixed set of attributes to define provider-specific configuration, without heading down the dark alley of denormalisation.

Socket.x solves this with a generic `Attribute` - a structure encompassing a key, a pair of optional min/max constraints, and an optional default value. An attribute is used as a key in a `Map`, the value being an `Object`. Attributes allow you to specify additional configuration options that are understood by specific providers. For example, the following snippet alters the number of threads and the buffer size used by Undertow.
```java
new XServerConfig()
.withAttributes(new AttributeMap()
                .with(UndertowAtts.IO_THREADS, 8)
                .with(UndertowAtts.BUFFER_SIZE, 65536))
```

Attributes are type-safe when used with the `AttributeMap` wrapper; the latter will ensure that the assigned value matches the attribute's component type, and will perform boundary validation on the given value.

`UndertowAtts` houses the known attributes for Undertow. There are equivalents for other providers - `JettyAtts` and `NettyAtts`.

**Note**: Provider-specific attributes are still in their infancy. To date, we've only added the absolute bare minimum, and there are lots yet to be done. Feel free to submit a PR.

## Loading with YConf
Socket.x has baked-in support for [YConf](https://github.com/obsidiandynamics/yconf), letting you bootstrap your application from a YAML or JSON configuration file. The snippet below shows a fairly complete sample `XServerConfig` represented in YAML.
```yaml
port: 8080
httpsPort: 8443
path: /echo
idleTimeoutMillis: 300000
pingIntervalMillis: 60000
scanIntervalMillis: 1000
highWaterMark: 1000
sslContextProvider:
  type: com.obsidiandynamics.socketx.ssl.CompositeSSLContextProvider
  keyManagerProvider:
    type: com.obsidiandynamics.socketx.ssl.JKSKeyManagerProvider
    location: cp://keystore-dev.jks
    storePassword: storepass
    keyPassword: keypass
  trustManagerProvider:
    type: com.obsidiandynamics.socketx.ssl.JKSTrustManagerProvider
    location: cp://keystore-dev.jks
    storePassword: storepass
servlets:
- path: /health/*
  name: health
  servletClass: sample.servlet.HealthCheckServlet  
attributes:
  socketx.undertow.ioThreads: 8
  socketx.undertow.bufferSize: 65536
```

To load the configuration, add the following few lines:
```java
XServerConfig serverConfig = new MappingContext()
    .withParser(new SnakeyamlParser())
    .fromStream(ResourceLocator.asStream(new URI("cp://sample-server-config.yaml")))
    .map(XServerConfig.class);
```

The complete code listing for the above example is located at `examples/src/main/java/config`. To use YConf with the [Snakeyaml](https://bitbucket.org/asomov/snakeyaml) parser, add `com.obsidiandynamics.yconf:yconf-snakeyaml:0.2.1` to your Gradle dependencies.

# Additional Topics
## Logging
Socket.x classes log using [SLF4J](https://www.slf4j.org/), under the package `com.obsidiandynamics.socketx`. For normal operation, it's recommended to leave the `INFO` level on.

## Binary messages
WebSockets support sending of binary frames using the same semantics as text frames, but sending the data bytes directly, without the UTF-8 encoding. (In fact, apart from the encoding and the opcode, there is little difference between the two.) To send and receive binary frames use `XEndpoint.send(ByteBuffer)` and `XEndpointListener.onBinary(XEndpoint, ByteBuffer)` respectively.

**Note**: There's one gotcha with binary messages, which doesn't apply to text. Socket.x accepts and provides a `ByteBuffer` for sending and receiving, which is a **mutable** data structure. It is the application's responsibility to ensure that the `ByteBuffer` instances aren't reused/recycled after calling `send()`. Failing to do so would violate thread safety and lead to race conditions, as the `ByteBuffer` is manipulated asynchronously, in a different thread to the caller.

## Send callback
The `send(String|ByteBuffer)` operation on `XEndpoint` is asynchronous - it returns immediately after queuing the message, to be sent by a background thread later. To learn of the eventual status of the queued message, you can call the overloaded variant of `send()`, specifying an `XSendCallback` implementation. `XSendCallback` handles three life-cycle events:
```java
void onComplete(XEndpoint endpoint);
void onError(XEndpoint endpoint, Throwable cause);
void onSkip(XEndpoint endpoint);
```

### `onComplete()`
Invoked when the message has been successfully sent, from the perspective of the sender. This doesn't imply that the message was received by the counter-party, or successfully processed, for that matter. (These types of guarantees are outside of WebSocket scope, and require explicit application-level support.)

### `onError()`
Invoked if the send operation threw an exception with the underlying provider. The `Throwable` cause is made available.

### `onSkip()`
Invoked if the send message was dropped due to a breach of the [high-water mark](#user-content-high-water-mark). This means that the message _will not_ be sent at this time. You still have the ability to retry the send operation at a later point, should you want to.

## Flow control
When building high-throughput WebSocket applications, one must consider scenarios where message producers and message consumers are operating at varying rates. This could be due to the difference in hardware, underlying resources, the time to process messages or network congestion. At network level, WebSockets naturally benefit from the underlying TCP/IP _sliding window_ flow control, ensuring the buffers in the protocol stack don't overflow and that packets aren't dropped. What happens at the application level is beyond the scope of WebSockets.

Asynchronous I/O is unequivocally better than its blocking counterpart when building web-scale applications. It does, however, miss out on one important quality that is intrinsic to blocking I/O - flow control. When a blocking I/O library can't send any more date, it exerts backpressure on the producer. By contrast, a non-blocking library has no affect on the producer, leading to a potential build-up of messages in the send queue. This can cause all sorts of problems, the most troublesome of which being heap exhaustion on the sending machine.

To prevent message build-up without blocking or imposing any type of flow control, Socket.x comes with [high-water mark](#user-content-high-water-mark) support - dropping messages when the outgoing queue reaches a certain size. This is disabled by default (as there is no sensible default HWM), and needs to be enabled explicitly. A HWM isn't the most elegant way of dealing with congestion, but it is very effective in high fan-out, broadcast-style messaging scenarios involving a large number of consumers, whereby a single producer serves the same messages to each consumer and the loss of a message isn't catastrophic. (In other words, messages are informative, rather than prescriptive, and the correctness of the consumer isn't dependent on receiving all messages.)

In some messaging scenarios, a HWM will not suffice. The producer may actually need to stop sending if the consumer is unable to keep up. This is particularly crucial when the WebSocket endpoint _is the source of truth_, and the consumer has no way of recovering from the message loss. In Socket.x this can be accomplished by querying the `getBacklog()` method of an `XEndpoint` prior to sending. The `getBacklog()` method returns the number of messages sitting in the outgoing queue, letting the application decide whether it is appropriate to send another message at that point in time.

An alternative way of achieving the same outcome is to use the `XSendCallback` hook, counting the number of confirmed messages _versus_ the total number of sent messages. In fact, this is roughly how the backlog counter and HWM mechanisms work behind the scenes.

## Servlet support
### A brief overview
Socket.x is focused on WebSocket applications. And while the underlying providers may (and typically do) support a broader spectrum of HTTP, Socket.x does not attempt to solve this problem for the complete set of HTTP use cases. Crucially, this would run contrary to the Socket.x design philosophy - to offer an **uncompromised, WebSocket-centric programming model**, no other strings attached. That said, we do acknowledge that being able to host a basic Servlet alongside your WebSocket application can be somewhat convenient, particularly when dealing with load balancers and service discovery proxies - you might want to expose a simple status or health check endpoint on the same port as your main WebSocket server. Taking this further, it would be doubly convenient to leverage this capability _generically_, without dealing directly with the underlying provider.

Socket.x adds **best-effort** Servlet 3.1 support. In other words, the Servlet 3.1 specification is supported _if and only if_ the underlying provider chooses to implement this, and it is under no obligation to do so in order to qualify as a fully-fledged Socket.x provider. Fortunately, both Undertow and Jetty providers support this feature. Netty, on the other hand, is not a Servlet container, and will throw an `UnsupportedOperationException` if you try to add a Servlet mapping.

### Adding a Servlet mapping
A Servlet mapping is defined in `XServerConfig` using the `withServlets()` method, as illustrated in the snippet below. The complete code listing for this example can be found in `examples/src/main/java/servlet`.
```java
// the Servlet definition
public static class HealthCheckServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;   

  @Override
  public void init(ServletConfig config) throws ServletException {
    System.out.format("Initialised with %s\n", config);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.getWriter().write("Cruizin'");
  }
}

// the main method (in a separate class)
public static void main(String[] args) throws Exception {
  XServer<?> server = UndertowServer
      .factory()
      .create(new XServerConfig()
              .withPath("/echo")
              .withPort(8080)
              .withHttpsPort(8443)
              .withSSLContextProvider(CompositeSSLContextProvider.getDevServerDefault())
              .withServlets(new XMappedServlet("/health/*", HealthCheckServlet.class)),
              new XEndpointLambdaListener<>());

  if (Desktop.isDesktopSupported()) {
    Desktop.getDesktop().browse(new URI("http://localhost:8080/health"));
  }

  Thread.sleep(60_000);
  server.close();
}
```

The `withServlets()` method takes in an a _varargs_ array of `XMappedServlet` objects. Each mapping specifies, at minimum, the path specification and the class of the Servlet - in our case a `HealthCheckServlet`. The life-cycle management of the Servlet is the responsibility of the Servlet container; it will be instantiated and initialised as required.

Run the example above. If running from the desktop, it should automatically navigate to `http://localhost:8080/health` with the default browser. You have 60 seconds before the application terminates.

**Note:** SSL configuration applies to _both_ WebSocket Secure and conventional HTTPS traffic. So `https://localhost:8443/health` will also work, albeit the browser will likely complain due to the untrusted nature of a self-signed certificate.

## Connection termination
The `XEndpoint.close()` method is normally used for initiating an orderly disconnection sequence using `CLOSE` frames, as per the [WebSocket RFC](https://tools.ietf.org/html/rfc6455). In some cases, a party may not have the luxury of waiting for the connection to close gracefully. For example, the connection may have been deemed as inactive, corrupt, or otherwise compromised. In this case it's better to close the connection forcibly, using the `terminate()` method.

The `XEndpointListener` has a pair of life-cycle methods - `onDisconnect(E endpoint, int statusCode, String reason)` and `onClose(E endpoint)` (where `E` is the concrete `XEndpoint` type). The `onClose()` method is always invoked when a connection is closed - cleanly or otherwise. The `onDisconnect()` method is invoked _if and only if_ the connection was closed gracefully, and in which case `onDisconnect()` is invoked before `onClose()`.

If you need to handle the end-state of the connection, with no regard for the close status code or reason, implementing `onClose()` is sufficient, as this will handle both cases.

## Utilities
Socket.x comes with a couple of utility classes that can come in handy when working with not only WebSockets, but other network sockets, protocols and I/O in general. It is expected that, over time, some of these utilities will be extracted out of Socket.x and turned into miniature libraries in their own right.

### `Timesert` and `Await`
These two utilities are often used together. `Await`'s methods block the calling thread until a certain condition, described by the specified `BooleanSupplier` evaluates to `true`. There are variations of the blocking methods - some return a `boolean`, indicating whether the condition has been satisfied with the allotted time frame, while others throw a `TimeoutException`. You can specify an upper bound on how long to wait for, as well as the checking interval (which otherwise defaults to 1 ms).  All times accepted by any of the methods are in milliseconds.

`Timesert` is a wrapper around `Await` that's useful for testing assertions and works well with frameworks such as JUnit and TestNG. You might notice that it's strikingly similar to Awaitility; however, `Timesert` is significantly more efficient and doesn't suffer from a subtle problem that has been shown to adversely affect Awaitility - a system clock that isn't monotonically non-decreasing. (While rare, this condition can happen when using NTP, and is particularly problematic on macOS.) `Timesert` is robust to negative clock drift. Having migrated all test cases (hundreds of them) from Awaitility to Timesert, we've noticed significantly higher CPU utilisation of unit tests (about 50% higher than baseline) and a corresponding reduction time in builds by about 30%.

`Await` and `Timesert` are useful when writing and testing network applications, as events don't happen instantly. For example, when sending a message you might want to assert that it has been received. But running an assertion on the receiver immediately following a send in an asynchronous environment will likely fail. Using `Timesert` allows for assertions to fail up to a certain point, after which the `AssertionError` is percolated to the caller and the test case fails. This way `Timesert` allows you to write efficient, reproducible assertions without resorting to `Thread.sleep()`.

**Note**: While working through the examples above, you would've already used `Await`, probably without realising it. The `drain()` method actually calls `Await.perpetual()` behind the scenes.

### `BinaryUtils`
Provides conversion and printing utilities for binary data (byte arrays). The `dump()` method is the most useful, converting a byte array into a multi-line hex dump, reminiscent of the output of popular hex editors. The following is an example of its output:
```
20 00 15 73 6F 6D 65 2F   74 6F 70 69 63 2F 74 6F
2F 70 75 62 6C 69 73 68   00 01 02
```

### `ResourceLocator`
Loads a specified resource URI as an `InputStream`. The URI can be of the form `file://...` - for reading files from the local file system, or `cp://...` - for reading files from the classpath (e.g. if the file has been packaged into an application JAR).

### `SocketUtils`
Utilities for working with TCP sockets. Notable methods include -

* `getAvailablePort(int preferredPort, int maxPort)` - searches for free (unbound) ports on the local machine, starting with the `preferredPort`, and up to the `maxPort`, inclusive. This method returns the first available port in the given range if one was found in a single complete pass. Alternatively, a `NoAvailablePortsException` unchecked exception is thrown if no unbound ports are available. Typically, this method is used when you need a spare port to bind to and where your application may have a preference for a specific port, but can still function correctly if a different port is assigned. This is often the case for testing scenarios.
* `isLocalPortAvailable(int port)` - tests whether the given port is bound on the local machine. Behind the scenes, the method attempts to bind on the port and, if successful, sets the `SO_REUSEADDR` socket option before yielding the port, so that it may be used immediately.
* `getPortUseCount(int port)` - used to query the number of uses of a given port, including the number of open socket connections, sockets in a `CLOSE_WAIT` state, as well as `LISTEN`. Effectively, this method counts the number of times a port appears in the output of a `netstat` command. Presently, this method relies on system utilities `netstat`, `grep` and `wc`, and can therefore only be used on a *NIX operating system, such as Linux, BSD, macOS, etc.
* `drainPort(int port, int maxUseCount, int drainIntervalMillis)` - blocks the calling thread until the number of uses of the given port reaches or drops below `maxUseCount`, effectively draining the port of open connections. The `drainIntervalMillis` parameter introduces waits between successive checks.

### `URIBuilder`
A fluent builder for incrementally assembling `URI` objects. The following snippet demonstrates its use:
```java
URI uri = URIBuilder.create()
    .withWebSocket(true)
    .withHttps(true)
    .withHost("pound")
    .withPath("/dog")
    .withPortProvider(getPorts())
    .build();
// produces 'wss://pound:8443/dog'
```


# Applications using Socket.x
The following is a list of applications using Socket.x that we're _aware_ of:

* [Flywheel](https://github.com/william-hill-community/flywheel) - a high-performance, distributed IoT message broker.

