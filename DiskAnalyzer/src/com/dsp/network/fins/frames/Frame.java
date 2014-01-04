package com.dsp.network.fins.frames;

public abstract class Frame {

  protected byte[] _frame;
  
  protected Frame(byte[] frame) {
    _frame = frame;
  }
  
  public byte[] getRawFrame() {
    return _frame;
  }
  
}
