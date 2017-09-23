package com.obsidiandynamics.socketx.attribute;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import com.obsidiandynamics.socketx.attribute.Attribute.*;

public final class AttributeTest {
  private static final String KEY = "key";
  
  @Test
  public void testToString() {
    assertEquals(KEY, new Attribute<>(KEY).toString());
  }

  @Test
  public void testValidateNoBounds() {
    final Attribute<Integer> att = new Attribute<>(KEY);
    att.validate(Collections.emptyMap(), 0);
  }

  @Test
  public void testValidateMinPass() {
    final Attribute<Integer> att = new Attribute<Integer>(KEY)
        .withMin(Constant.of(10));
    att.validate(Collections.emptyMap(), 10);
  }

  @Test(expected=IllegalAttributeException.class)
  public void testValidateMinFail() {
    final Attribute<Integer> att = new Attribute<Integer>(KEY)
        .withMin(Constant.of(10));
    att.validate(Collections.emptyMap(), 9);
  }

  @Test
  public void testValidateMaxPass() {
    final Attribute<Integer> att = new Attribute<Integer>(KEY)
        .withMax(Constant.of(20));
    att.validate(Collections.emptyMap(), 20);
  }

  @Test(expected=IllegalAttributeException.class)
  public void testValidateMaxFail() {
    final Attribute<Integer> att = new Attribute<Integer>(KEY)
        .withMax(Constant.of(20));
    att.validate(Collections.emptyMap(), 21);
  }
  
  @Test
  public void testGetNonNull() {
    final Attribute<Integer> att = new Attribute<Integer>(KEY);
    final Integer value = att.get(new AttributeMap().with(att, 5));
    assertNotNull(value);
    assertEquals(5, (int) value);
  }
  
  @Test
  public void testGetNullWithDefault() {
    final Integer value = new Attribute<Integer>(KEY).withDefault(Constant.of(7)).get(Collections.emptyMap());
    assertNotNull(value);
    assertEquals(7, (int) value);
  }
  
  @Test
  public void testGetNullWithoutDefault() {
    final Integer value = new Attribute<Integer>(KEY).get(Collections.emptyMap());
    assertNull(value);
  }
  
  @Test
  public void testSet() {
    final Attribute<Integer> att = new Attribute<>(KEY);
    final Map<String, Object> atts = new HashMap<>();
    att.set(atts, 42);
    assertEquals(42, atts.get(KEY));
  }
}
