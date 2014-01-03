package com.dsp.network.exceptions;

public class UnknownMemoryAreaException extends Exception {
  
  private static final long serialVersionUID = -4128729662371030197L;
  
  private String _area;

  public UnknownMemoryAreaException(String area) {
    _area = area;
  }
  
  @Override
  public String getMessage() {
    return "Unknown Memory Area: " + _area;
  }
  
}
