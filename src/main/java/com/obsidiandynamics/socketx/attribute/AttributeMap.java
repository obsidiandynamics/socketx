package com.obsidiandynamics.socketx.attribute;

import java.util.*;

/**
 *  A map for storing attributes, providing a convenient chained method API.
 */
public final class AttributeMap extends LinkedHashMap<String, Object> {
  private static final long serialVersionUID = 1L;

  /**
   *  Assigns a value to the given attribute, performing attribute-specific validation
   *  in the process.
   *  
   *  @param <T> The value type.
   *  @param attribute The attribute.
   *  @param value The value to set.
   *  @return The current {@link AttributeMap} instance for chaining.
   */
  public <T extends Comparable<T>> AttributeMap with(Attribute<T> attribute, T value) {
    attribute.set(this, value);
    return this;
  }
}
