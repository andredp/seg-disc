package com.dsp.libs;

public class Utils {

  /**
   * 
   * @param value
   * @param dec_cases
   * @return
   */
  public static int doubleToDInt(double value, int dec_cases) {
    return (int) Math.round((value * Math.pow(10, dec_cases)));
  } 
  
  /**
   * 
   * @param word
   * @return
   */
  public static double decimalIntToDouble(int word, int decimal_cases) {
    return (double) word / Math.pow(10, decimal_cases);
  }
  
  /**
   * DEPRECATED! Use the other one (it's less heap intensive)
   * @param decimal
   * @return
   */
  public static byte[] decToHexBytes(int decimal) {
    return new byte[] {
      (byte) ((decimal & 0xff000000) >> 24),
      (byte) ((decimal & 0x00ff0000) >> 16),
      (byte) ((decimal & 0x0000ff00) >>  8),
      (byte)  (decimal & 0x000000ff)
    };
  }
  
  /**
   * 
   * @param decimal
   * @param nth
   * @return
   */
  public static byte decToHexBytes(int decimal, int nth) {
    int shift = Integer.SIZE - ((nth + 1) * Byte.SIZE);
    return (byte) ((decimal & (0xff << shift)) >> shift);
  }
  
  /**
   * 
   * @param high_part
   * @param low_part
   * @return
   */
  public static double dIntToDouble(byte high_part, byte low_part, int decimal_cases) {
    int word = (((int)high_part & 0xff) << 8) | ((int)low_part & 0xff);
    return Utils.decimalIntToDouble(word, decimal_cases);
  }
  
}
