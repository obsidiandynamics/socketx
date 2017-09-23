package com.obsidiandynamics.socketx.util;

import java.lang.reflect.*;
import java.util.*;

import org.slf4j.*;

import com.obsidiandynamics.indigo.util.*;
import com.obsidiandynamics.indigo.util.InterceptingProxy.*;

public final class Slf4jLoggingInterceptor<T> implements InvocationObserver<T>, TestSupport {
  private final Logger logger;
  
  private final String prefix;
  
  public Slf4jLoggingInterceptor(Logger logger) {
    this(logger, "");
  }
  
  public Slf4jLoggingInterceptor(Logger logger, String prefix) {
    this.logger = logger;
    this.prefix = prefix;
  }

  @Override
  public void onInvoke(T delegate, Method method, Object[] args, Object ret) {
    if (logger.isDebugEnabled()) {
      final boolean isVoid = method.getReturnType() == Void.TYPE;
      final String argsArray = args == null ? "" : arrayToString(args);
      if (isVoid) {
        logger.debug(String.format("%s%s(%s) => void\n", prefix, method.getName(), argsArray));
      } else {
        logger.debug(String.format("%s%s(%s) => %s\n", prefix, method.getName(), argsArray, ret));
      }
    }
  }
  
  private static String arrayToString(Object[] array) {
    final String rawStr = Arrays.toString(array);
    return rawStr.substring(1, rawStr.length() - 1);
  }
}