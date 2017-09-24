package com.obsidiandynamics.socketx.netty;

import com.obsidiandynamics.socketx.*;

public final class RunNetty {
  public static void main(String[] args) throws Exception {
    System.setProperty("io.netty.noUnsafe", Boolean.toString(true));
    final XServer<NettyEndpoint> netty = NettyServer.factory().create(new XServerConfig() {{
      port = 8080;
      path = "/";
    }}, null);
    netty.close();
  }
}
