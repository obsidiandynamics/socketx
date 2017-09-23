package com.obsidiandynamics.socketx.attribute;

import java.util.*;

public final class Constant<T> implements Attribute.ValueSupplier<T> {
  private final T value;
  
  private Constant(T value) {
    this.value = value;
  }
  
  @Override
  public T get(Map<String, Object> options) {
    return value;
  }

  public static <T> Constant<T> of(T value) {
    return new Constant<>(value);
  }
}
