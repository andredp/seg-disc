package com.dsp.network.fins.frames;

import com.dsp.libs.Utils;

public class FINSTCPHeaderSendFrame extends FINSFrame {
  
  private static final byte[] TEMPLATE = new byte[] {
    (byte) 'F',  (byte) 'I',  (byte) 'N',  (byte) 'S',
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Length
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Command
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00  // Error Code
  };
  
  public FINSTCPHeaderSendFrame() {
    super(TEMPLATE.clone());
  }
  
  public byte getErrorCode() { return _frame[ERRC_3]; }

  public void setLength(int length) {
    byte[] hex_length  = Utils.decToHexBytes(length);
    _frame[LNGT_0] = hex_length[0];
    _frame[LNGT_1] = hex_length[1];
    _frame[LNGT_2] = hex_length[2];
    _frame[LNGT_3] = hex_length[3];
  }
  
  public int getDataLength() {
    int length = (((int) _frame[LNGT_2] & 0xff) << 8) | ((int) _frame[LNGT_3] & 0xff);
    length -= (FINSTCPHeaderSendFrame.frameLength() + 
               FINSHeaderFrame.frameLength() +
               FINSCommandResponseFrame.frameLength());
    return length;
  }
  
  public boolean hasError() {
    return (_frame[ERRC_2] << 8 | _frame[ERRC_3]) != 0;
  }

  public static int frameLength() {
    // length from command field to the end
    return TEMPLATE.length - COMM_0; 
  }
  
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
  private static final int ERRC_2 = 14;
  private static final int ERRC_3 = 15;
  
}
