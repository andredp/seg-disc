package com.dsp.analyzer;

public class DiscRawData {
  
  private double[] _front;
  
  private double[] _back;

  public DiscRawData(double[] front, double[] back) {
    _front = front;
    _back = back;
  }
  
  public double[] getFront() {
    return _front;
  }

  public double[] getBack() {
    return _back;
  }
}
