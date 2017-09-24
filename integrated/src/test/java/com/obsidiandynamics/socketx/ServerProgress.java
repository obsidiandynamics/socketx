package com.obsidiandynamics.socketx;

@FunctionalInterface
interface ServerProgress {
  void update(ServerHarness server, long sent);
}
