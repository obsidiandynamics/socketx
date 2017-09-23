package com.obsidiandynamics.socketx.attribute;

import java.util.*;

/**
 *  A map for storing attributes, providing a convenient chained method API.
 */
public final class AttributeMap extends LinkedHashMap<String, Object> {
  private static final long serialVersionUID = 1L;

  public <T extends Comparable<T>> AttributeMap with(Attribute<T> option, T value) {
    option.set(this, value);
    return this;
  }
}
