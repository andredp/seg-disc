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
  
}
