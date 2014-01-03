package com.dsp.communicators.fins.frames;

public abstract class FINSFrame {

  protected byte[] _frame;
  
  protected FINSFrame(byte[] frame) {
    _frame = frame;
  }
  
  public byte[] getRawFrame() {
    return _frame;
  }
  
}
