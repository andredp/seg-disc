package com.dsp.network.fins.frames;

import com.dsp.libs.Utils;
import com.dsp.network.exceptions.UnknownCommandTypeException;
import com.dsp.network.exceptions.UnknownMemoryAreaException;

public class FINSCommandFrame extends Frame {

  private static final byte[] TEMPLATE = {
    // Command 
    (byte) 0x00, (byte) 0x00, // Command Code
    // Parameters
    (byte) 0x00,              // Memory area designation
    (byte) 0x00, (byte) 0x00, // Beginning address
    (byte) 0x00,              // Beginning bit (0x00 if a word is to be read)
    (byte) 0x00, (byte) 0x00  // Amount to read (999 is the max in Ethernet)
  };
  
  public FINSCommandFrame() {
    super(TEMPLATE.clone());
  }
  
  public void setMemArea(String area) throws UnknownMemoryAreaException {
    if (area.equalsIgnoreCase("D")) { _frame[AREA] = (byte) 0x82; return; }
    // enter other areas...
    throw new UnknownMemoryAreaException(area);
  }
  
  public void setCommandCode(CommandType type) throws UnknownCommandTypeException {
    switch (type) {
    case AREA_READ: 
      _frame[COMM_H] = (byte) 0x01; 
      _frame[COMM_L] = (byte) 0x01; 
      return;
    case AREA_WRITE: 
      _frame[COMM_H] = (byte) 0x01; 
      _frame[COMM_L] = (byte) 0x02; 
      return;
    default: throw new UnknownCommandTypeException(type.toString());
    }
  }
  
  public void setMemAddress(int address) {
    _frame[ADDR_H]  = Utils.decToHexBytes(address, 2);
    _frame[ADDR_L]  = Utils.decToHexBytes(address, 3);
  }
  
  public void setNumWords(int words) {
    _frame[WRDS_H] = Utils.decToHexBytes(words, 2);
    _frame[WRDS_L] = Utils.decToHexBytes(words, 3);
  }
  
  public void prepareFrame(CommandType type, String area, int address, int amount) 
      throws UnknownMemoryAreaException, UnknownCommandTypeException {
    setMemArea(area);
    setCommandCode(type);
    setMemAddress(address);
    setNumWords(amount);
  }
  
  public static int frameLength() {
    return TEMPLATE.length;
  }
  
  // command type enum
  public enum CommandType {
    AREA_WRITE, AREA_READ
  };

  // fields indexes
  private static final int COMM_H = 0;
  private static final int COMM_L = 1;
  private static final int AREA   = 2;
  private static final int ADDR_H = 3; 
  private static final int ADDR_L = 4;
//private static final int BIT    = 5;
  private static final int WRDS_H = 6; 
  private static final int WRDS_L = 7;
  
}
