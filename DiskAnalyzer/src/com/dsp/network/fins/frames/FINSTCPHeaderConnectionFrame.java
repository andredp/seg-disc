package com.dsp.network.fins.frames;

import com.dsp.libs.Utils;

public class FINSTCPHeaderConnectionFrame extends FINSFrame {

  private static final byte[] TEMPLATE = new byte[] {
    (byte) 'F',  (byte) 'I',  (byte) 'N',  (byte) 'S',
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C, // Length
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Command
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Error Code (Always 0x00)
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Client node (0 = automatic)
  };
  
  public FINSTCPHeaderConnectionFrame() {
    super(TEMPLATE.clone());
  }
  
  public void setClientNode(byte node) { _frame[CLIN_3] = node; }
  public byte getClientNode()          { return _frame[CLIN_3]; }
  
  public void setLength(int length) {
    byte[] hex_length  = Utils.decToHexBytes(length);
    _frame[LNGT_0] = hex_length[0];
    _frame[LNGT_1] = hex_length[1];
    _frame[LNGT_2] = hex_length[2];
    _frame[LNGT_3] = hex_length[3];
  }

  public static int frameLength() {
    return TEMPLATE.length - COMM_0;
  }
  
  // frame indexes
  private static final int LNGT_0 = 4;
  private static final int LNGT_1 = 5;
  private static final int LNGT_2 = 6;
  private static final int LNGT_3 = 7;
  private static final int COMM_0 = 8;
//private static final int COMM_1 = 9;
//private static final int COMM_2 = 10;
//private static final int COMM_3 = 11;
//private static final int ERRC_0 = 12;
//private static final int ERRC_1 = 13;
//private static final int ERRC_2 = 14;
//private static final int ERRC_3 = 15;
//private static final int CLIN_0  = 16;
//private static final int CLIN_1  = 17;
//private static final int CLIN_2  = 18;
  private static final int CLIN_3  = 19;
  

}
