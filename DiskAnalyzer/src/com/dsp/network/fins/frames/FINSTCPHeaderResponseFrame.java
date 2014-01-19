package com.dsp.network.fins.frames;

import com.dsp.libs.Utils;

public class FINSTCPHeaderResponseFrame extends Frame {
  
  private static final byte[] TEMPLATE = new byte[] {
    (byte) 'F',  (byte) 'I',  (byte) 'N',  (byte) 'S',
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x16, // Length  (0x14 to 0x7E4)
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, // Command (0x06 - OK / 0x03 - NOK) 
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00  // Error Code
  };
  
  public FINSTCPHeaderResponseFrame() {
    super(TEMPLATE.clone());
  }
  
  public byte getErrorCode() { return _frame[ERRC]; }
  
  /**
   * 
   * @return the length of the raw data (excluding the frames) that the PLC sent
   */
  public int getDataLength() {
    int length = Utils.bytesToInt(_frame[LNGT_H], _frame[LNGT_L]);
    length -= (FINSTCPHeaderResponseFrame.frameLength() + FINSHeaderFrame.frameLength() 
        + FINSCommandResponseFrame.frameLength());
    return length;
  }
  
  public boolean hasError() {
    //return _frame[COMM] != (byte) 0x06; // (0x06 is a connection confirmation command)
    return _frame[ERRC] != (byte) 0x00;
  }

  public static int frameLength() {
    return 8; // length from command field to the end
  }
  
  public String getErrorMessage() throws Exception {
    switch (_frame[ERRC]) {
    case 0x00: return "Normal (no error).";
    case 0x01: return "The header is not 'FINS' (ASCII code).";
    case 0x02: return "The data length is too long.";
    case 0x03: return "The command is not supported.";
    default:   throw new Exception("Unkown error code found in the frame.");
    }
  }
  
  // indexes
  private static final int LNGT_H = 6;
  private static final int LNGT_L = 7;
//private static final int COMM   = 11;
  private static final int ERRC   = 15;
  
}
