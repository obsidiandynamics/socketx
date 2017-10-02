package com.obsidiandynamics.socketx.util;

import java.nio.*;
import java.util.*;

/**
 *  Provides conversion and printing utilities for binary data (byte arrays).
 */
public final class BinaryUtils {
  private BinaryUtils() {}
  
  /**
   *  Converts a given {@link ByteBuffer} to a byte array.
   *  
   *  @param buf The buffer to convert.
   *  @return The resulting byte array.
   */
  public static byte[] toByteArray(ByteBuffer buf) {
    final int pos = buf.position();
    final byte[] bytes = new byte[buf.remaining()];
    buf.get(bytes);
    buf.position(pos);
    return bytes;
  }
  
  /**
   *  A variant of {@link #dump} that works on a {@link ByteBuffer}.
   *  
   *  @param buf The buffer.
   *  @return The formatted string, potentially containing newline characters.
   */
  public static String dump(ByteBuffer buf) {
    return dump(toByteArray(buf));
  }
  
  /**
   *  Dumps the contents of the given byte array to a formatted hex string, using a multi-line,
   *  8 + 8 layout commonly used in hex editors.<p>
   *  
   *  A typical hex dump resembles the following:<br>
   *  {@code
   *  20 00 15 73 6F 6D 65 2F   74 6F 70 69 63 2F 74 6F
   *  2F 70 75 62 6C 69 73 68   00 01 02
   *  }
   *  
   *  @param bytes The byte array.
   *  @return The formatted string, potentially containing newline characters.
   */
  public static String dump(byte[] bytes) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < bytes.length; i++) {
      sb.append(toHex(bytes[i]));
      if (i != bytes.length - 1) {
        if (i % 16 == 15) {
          sb.append('\n');
        } else if (i % 8 == 7) {
          sb.append("   ");
        } else {
          sb.append(' ');
        }
      }
    }
    return sb.toString();
  }
  
  /**
   *  Converts a given (unsigned) byte to a pair of hex characters, zero-padded if
   *  the value is lower than 0x10.
   *  
   *  @param b The byte to convert.
   *  @return The hex string.
   */
  public static String toHex(byte b) {
    final String str = Integer.toHexString(Byte.toUnsignedInt(b)).toUpperCase();
    return str.length() < 2 ? "0" + str : str;
  }
  
  /**
   *  Converts a varargs array of integers into a byte array, where the integers are
   *  assumed to be holding an unsigned byte value.
   *  
   *  @param ints The ints to convert.
   *  @return The resulting byte array.
   */
  public static byte[] toByteArray(int ... ints) {
    final byte[] bytes = new byte[ints.length];
    for (int i = 0; i < ints.length; i++) {
      bytes[i] = (byte) ints[i];
    }
    return bytes;
  }
  
  /**
   *  Converts a varargs array of integers into a {@link ByteBuffer}, where the integers are
   *  assumed to be holding an unsigned byte value.
   *  
   *  @param ints The ints to convert.
   *  @return The resulting {@link ByteBuffer}.
   */
  public static ByteBuffer toByteBuffer(int ... ints) {
    final byte[] bytes = toByteArray(ints);
    return ByteBuffer.wrap(bytes);
  }
  
  /**
   *  Produces an array of random bytes.
   *  
   *  @param length The length of the array.
   *  @return The random byte array.
   */
  public static byte[] randomBytes(int length) {
    final byte[] bytes = new byte[length];
    new Random().nextBytes(bytes);
    return bytes;
  }
  
  /**
   *  Produces a random hex string, where each character is between '0' and 'F'.
   *  
   *  @param length The length of the string; must be a multiple of two.
   *  @return The random hex string.
   */
  public static String randomHexString(int length) {
    if (length % 2 != 0) throw new IllegalArgumentException("Length must be a multiple of 2");
    final StringBuilder sb = new StringBuilder(length);
    final byte[] bytes = randomBytes(length / 2);
    for (int i = 0; i < bytes.length; i++) {
      sb.append(toHex(bytes[i]));
    }
    return sb.toString();
  }
}
