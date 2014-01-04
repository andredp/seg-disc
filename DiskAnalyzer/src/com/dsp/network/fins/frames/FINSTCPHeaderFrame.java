package com.dsp.network.fins.frames;

import com.dsp.libs.Utils;

public class FINSTCPHeaderFrame extends Frame {
  
  private static final byte[] TEMPLATE = new byte[] {
    (byte) 'F',  (byte) 'I',  (byte) 'N',  (byte) 'S',
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x16, // Length (0x14 to 0x7E4)
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Command
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00  // Error Code (not used)
  };
  
  public FINSTCPHeaderFrame() {
    super(TEMPLATE.clone());
  }

  public void setLength(int length) {
    _frame[LNGT_H] = Utils.decToHexBytes(length, 2);
    _frame[LNGT_L] = Utils.decToHexBytes(length, 3);
  }

  public static int frameLength() {
    return 8; // length from command field to the end
  }
  
  // indexes
  private static final int LNGT_H = 6;
  private static final int LNGT_L = 7;
  
}
