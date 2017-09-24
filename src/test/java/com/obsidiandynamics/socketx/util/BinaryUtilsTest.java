package com.obsidiandynamics.socketx.util;

import static com.obsidiandynamics.socketx.util.BinaryUtils.*;
import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

public final class BinaryUtilsTest {
  @Test
  public void testDumpSmall() {
    assertEquals("00 01 02 03 04 05 06 07", 
                 dump(toByteArray(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07)));
  }
  
  @Test
  public void testDumpMedium() {
    assertEquals("00 01 02 03 04 05 06 07   08 09 0A 0B 0C 0D 0E 0F", 
                 dump(toByteArray(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 
                                  0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F)));
  }
  
  @Test
  public void testDumpLarge() {
    assertEquals("00 01 02 03 04 05 06 07   08 09 0A 0B 0C 0D 0E 0F\n" +
                 "10 11 12", 
                 dump(toByteBuffer(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 
                                   0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
                                   0x10, 0x11, 0x12)));
  }
  
  @Test
  public void testToByteArray() {
    assertArrayEquals(new byte[] {(byte) 0x7F, (byte) 0x80, (byte) 0x81}, 
                      toByteArray(0x7F, 0x80, 0x81));
  }
  
  @Test
  public void testToHex() {
    assertEquals("00", toHex((byte) 0x00));
    assertEquals("7F", toHex((byte) 0x7F));
    assertEquals("80", toHex((byte) 0x80));
    assertEquals("FF", toHex((byte) 0xFF));
  }
  
  @Test
  public void testConformance() throws Exception {
    TestSupport.assertUtilityClassWellDefined(BinaryUtils.class);
  }
  
  @Test
  public void testRandomBytes() {
    final byte[] bytes = BinaryUtils.randomBytes(8);
    assertEquals(8, bytes.length);
  }
  
  @Test
  public void testRandomHexString() {
    final String str = BinaryUtils.randomHexString(8);
    assertEquals(8, str.length());
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testRandomHexStringOddLength() {
    BinaryUtils.randomHexString(7);
  }
}
