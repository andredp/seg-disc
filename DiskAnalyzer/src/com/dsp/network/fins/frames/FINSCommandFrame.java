package com.dsp.network.fins.frames;

import com.dsp.libs.Utils;
import com.dsp.network.exceptions.UnknownCommandTypeException;
import com.dsp.network.exceptions.UnknownMemoryAreaException;

public class FINSCommandFrame extends FINSFrame {

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
  
  public void setCommandCode(String type) throws UnknownCommandTypeException {
    if (type.equalsIgnoreCase("area_read"))  { _frame[COMM_0] = (byte) 0x01; _frame[COMM_1] = (byte) 0x01; return; }
    if (type.equalsIgnoreCase("area_write")) { _frame[COMM_0] = (byte) 0x01; _frame[COMM_1] = (byte) 0x02; return; }
    // enter other commands...
    throw new UnknownCommandTypeException(type);
  }
  
  public void setMemAddress(int address) {
    _frame[ADDR_0]  = Utils.decToHexBytes(address, 2);
    _frame[ADDR_1]  = Utils.decToHexBytes(address, 3);
  }
  
  public void setNumWords(int words) {
    _frame[WRDS_0] = Utils.decToHexBytes(words, 2);
    _frame[WRDS_1] = Utils.decToHexBytes(words, 3);
  }
  
  public void prepareFrame(String type, String area, int address, int amount) 
      throws UnknownMemoryAreaException, UnknownCommandTypeException {
    setMemArea(area);
    setCommandCode(type);
    setMemAddress(address);
    setNumWords(amount);
  }
  
  public static int frameLength() {
    return TEMPLATE.length;
  }

  // fields indexes
  private static final int COMM_0 = 0;
  private static final int COMM_1 = 1;
  private static final int AREA   = 2;
  private static final int ADDR_0 = 3; 
  private static final int ADDR_1 = 4;
//private static final int BIT    = 5;
  private static final int WRDS_0 = 6; 
  private static final int WRDS_1 = 7;
  
}
