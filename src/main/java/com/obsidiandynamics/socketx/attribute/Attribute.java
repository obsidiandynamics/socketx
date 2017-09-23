package com.obsidiandynamics.socketx.attribute;

import static com.obsidiandynamics.socketx.attribute.Attribute.ValueSupplier.*;

import java.util.*;

/**
 *  A strongly-typed value that may be passed as a configuration to an 
 *  {@link com.obsidiandynamics.socketx.XClient} or
 *  an {@link com.obsidiandynamics.socketx.XServer}, where the configuration is 
 *  implementation-specific.
 *
 *  @param <T> Value type.
 */
public final class Attribute<T extends Comparable<T>> {
  /**
   *  A way of resolving a value, which may depend on peer values in the
   *  attribute map.
   *
   *  @param <T> Value type.
   */
  @FunctionalInterface
  public interface ValueSupplier<T> {
    T get(Map<String, Object> atts);
    
    @SuppressWarnings("unchecked")
    static <T> T cast(Object obj) {
      return (T) obj;
    }
  }
  
  /**
   *  Thrown if an attribute value fails validation.
   */
  public static final class IllegalAttributeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    IllegalAttributeException(String m) { super(m); }
  }
  
  private final String key;
  
  private ValueSupplier<T> min;
  
  private ValueSupplier<T> max;
  
  private ValueSupplier<T> def;
  
  public Attribute(String key) {
    this.key = key;
  }
  
  public Attribute<T> withMin(ValueSupplier<T> min) {
    this.min = min;
    return this;
  }

  public Attribute<T> withMax(ValueSupplier<T> max) {
    this.max = max;
    return this;
  }

  public Attribute<T> withDefault(ValueSupplier<T> def) {
    this.def = def;
    return this;
  }
  
  public T get(Map<String, Object> atts) {
    final Object value = atts.get(key);
    final T resolved = cast(value != null ? value : def != null ? def.get(atts) : null);
    if (resolved == null) return null;
    validate(atts, resolved);
    return resolved;
  }
  
  public T set(Map<String, Object> atts, T value) {
    validate(atts, value);
    return cast(atts.put(key, value));
  }
  
  public void validate(Map<String, Object> atts, T value) {
    if (min != null) {
      final T minValue = min.get(atts);
      if (value.compareTo(minValue) < 0) {
        throw new IllegalAttributeException(String.format("Value %s is smaller than the minimum %s", value, minValue));
      }
    }
    
    if (max != null) {
      final T maxValue = max.get(atts);
      if (value.compareTo(maxValue) > 0) {
        throw new IllegalAttributeException(String.format("Value %s is larger than the maximum %s", value, maxValue));
      }
    }
  }

  @Override
  public String toString() {
    return key;
  }
}
