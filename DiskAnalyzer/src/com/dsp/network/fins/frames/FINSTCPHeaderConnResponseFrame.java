package com.dsp.network.fins.frames;

public class FINSTCPHeaderConnResponseFrame extends FINSFrame {

  private static final byte[] TEMPLATE = new byte[] {
    (byte) 'F',  (byte) 'I',  (byte) 'N',  (byte) 'S',
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Length
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Command
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Error Code
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Client node
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Server node
  };
  
  public FINSTCPHeaderConnResponseFrame() {
    super(TEMPLATE.clone());
  }
  
  public byte getClientNode() { return _frame[CLIN_3]; }
  public byte getServerNode() { return _frame[SRVN_3]; }
  public byte getErrorCode()  { return _frame[ERRC_3]; }
  
  public boolean hasError() {
    return ((_frame[ERRC_2] << 8) | _frame[ERRC_3]) != 0;
  }
  
  public static int frameLength() {
    return TEMPLATE.length;
  }

  // frame indexes
//private static final int LNGT_0 = 4;
//private static final int LNGT_1 = 5;
//private static final int LNGT_2 = 6;
//private static final int LNGT_3 = 7;
//private static final int COMM_0 = 8;
//private static final int COMM_1 = 9;
//private static final int COMM_2 = 10;
//private static final int COMM_3 = 11;
//private static final int ERRC_0 = 12;
//private static final int ERRC_1 = 13;
  private static final int ERRC_2 = 14;
  private static final int ERRC_3 = 15;
//private static final int CLIN_0 = 16;
//private static final int CLIN_1 = 17;
//private static final int CLIN_2 = 18;
  private static final int CLIN_3 = 19;
//private static final int SRVN_0 = 20;
//private static final int SRVN_1 = 21;
//private static final int SRVN_2 = 22;
  private static final int SRVN_3 = 23;
  
}
