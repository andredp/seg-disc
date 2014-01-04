package com.dsp.network.fins.frames;

public class FINSTCPConnectionFrame extends Frame {

  private static final byte[] TEMPLATE = new byte[] {
    (byte) 'F',  (byte) 'I',  (byte) 'N',  (byte) 'S',
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C, // Length
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Command
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Error Code (not used)
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Client node (0 = automatic)
  };
  
  public FINSTCPConnectionFrame() {
    super(TEMPLATE.clone());
  }
  
  public void setClientNode(byte node) { _frame[CLIN] = node; }
  public byte getClientNode()          { return _frame[CLIN]; }

  public static int frameLength() {
    return 12; // 0x0C
  }
  
  // frame indexes
  private static final int CLIN = 19;
  
}
