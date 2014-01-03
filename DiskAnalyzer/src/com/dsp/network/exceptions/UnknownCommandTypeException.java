package com.dsp.network.exceptions;

public class UnknownCommandTypeException extends Exception {
  
  private static final long serialVersionUID = -4128729662371030197L;
  
  private String _type;

  public UnknownCommandTypeException(String type) {
    _type = type;
  }
  
  @Override
  public String getMessage() {
    return "Unknown Command Type: " + _type;
  }
  
}
