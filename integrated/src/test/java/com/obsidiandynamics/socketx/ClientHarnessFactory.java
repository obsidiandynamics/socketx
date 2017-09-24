package com.obsidiandynamics.socketx;

import com.obsidiandynamics.socketx.util.URIBuilder.*;

@FunctionalInterface
interface ClientHarnessFactory {
  ClientHarness create(Ports ports, boolean https, boolean echo) throws Exception;
}
