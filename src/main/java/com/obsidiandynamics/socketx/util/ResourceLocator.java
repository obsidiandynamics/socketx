package com.obsidiandynamics.socketx.util;

import java.io.*;
import java.net.*;

public final class ResourceLocator {
  private ResourceLocator() {}
  
  public static InputStream asStream(URI uri) throws FileNotFoundException {
    switch (uri.getScheme()) {
      case "file":
        return new FileInputStream(new File(uri.getHost() + uri.getPath()));
        
      case "cp":
      case "classpath":
        return ResourceLocator.class.getClassLoader().getResourceAsStream(uri.getHost() + uri.getPath());
        
      default:
        throw new IllegalArgumentException("Unsupported URI scheme " + uri.getScheme());
    }
  }
}
