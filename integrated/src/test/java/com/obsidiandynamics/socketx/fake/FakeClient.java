package com.obsidiandynamics.socketx.fake;

import java.io.*;
import java.net.*;

import javax.net.*;

import com.obsidiandynamics.socketx.util.*;

public final class FakeClient extends Thread implements Closeable {
  private static final int BUFFER_SIZE = 8192;
  
  private final int expectedMessageSize;
  private final FakeClientCallback callback;
  
  private final Socket socket;
  
  private final byte[] buffer;
  
  public FakeClient(String path, int port, int expectedMessageSize, FakeClientCallback callback) throws UnknownHostException, IOException {
    super("FakeClient");
    this.expectedMessageSize = expectedMessageSize;
    this.callback = callback;
    socket = SocketFactory.getDefault().createSocket("localhost", port);
    
    final PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    writer.println("GET " + path + " HTTP/1.1\r");
    writer.println("Accept-Encoding: gzip\r");
    writer.println("User-Agent: fake\r");
    writer.println("Upgrade: websocket\r");
    writer.println("Connection: Upgrade\r");
    writer.println("Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==\r");
    writer.println("Origin: http://example.com\r");
    writer.println("Sec-WebSocket-Protocol: chat, superchat\r");
    writer.println("Sec-WebSocket-Version: 13\r");
    writer.println("Pragma: no-cache\r");
    writer.println("Cache-Control: no-cache\r");
    writer.println("Host: localhost:" + port + "\r");
    writer.println("\r");
    writer.flush();
    
    final InputStream in = socket.getInputStream();
    final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String line;
    while ((line = reader.readLine()) != null) {
      line = reader.readLine();
      if (line.isEmpty()) break;
    }
    callback.connected();
    buffer = new byte[BUFFER_SIZE];
    start();
  }
  
  @Override
  public void run() {
    final InputStream in;
    
    try {
      in = socket.getInputStream();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    
    int total = 0;
    final int frameSize = expectedMessageSize + 2;
    for (;;) {
      try {
        final int read = in.read(buffer);
        if (read != -1) {
          total += read;
          if (total >= frameSize) {
            final int messages = total / frameSize;
            total %= frameSize;
            callback.received(messages);
          }
        } else {
          break;
        }
      } catch (SocketException e) {
        if (socket.isClosed()) {
          break;
        } else {
          e.printStackTrace();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    callback.disconnected();
    
    if (! socket.isClosed()) {
      try {
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void close() throws IOException {
    if (! socket.isClosed()) {
      final byte[] closeFrame = BinaryUtils.toByteArray(0x88, 0x80, 0x98, 0xec, 0x87, 0xc0);
      try {
        socket.getOutputStream().write(closeFrame);
        socket.getOutputStream().flush();
        socket.getInputStream().read(buffer); // read the server's close frame
      } catch (IOException e) {
        if (! socket.isClosed()) e.printStackTrace();
      }
      socket.close();
    }
  }
}
