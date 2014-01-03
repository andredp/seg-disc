package com.dsp.network.fins.frames;


public class FINSCommandResponseFrame extends FINSFrame {

  public static final byte[] TEMPLATE = {
    // Command
    (byte) 0x01, (byte) 0x01,
    // End Code
    (byte) 0x00, (byte) 0x00, // 0x0000 means everything was OK
    // Data
  //(byte) 0x00, (byte) 0x00  // ... each word has 2 bytes (PC1L)
    // ...
  };
  
  private byte[] _data;
  
  public FINSCommandResponseFrame() {
    super(TEMPLATE.clone());
  }
  
  public void prepareDataBuffer(int length) {
    if (_data == null || _data.length != length) {
      _data = new byte[length];
    }
  }
  
  public byte[] getDataBuffer() { return _data; }
  
  public boolean hasError() {
    return (_frame[ENDC_0] << 8 | _frame[ENDC_1]) != 0;
  }
  
  public static int frameLength() {
    return TEMPLATE.length;
  }
  
  public int dataLength() {
    return _data.length;
  }
  
  // FINS Response fields indexes
//private static final int COMM_0 = 0;
//private static final int COMM_1 = 1;
  private static final int ENDC_0 = 2;
  private static final int ENDC_1 = 3;

}
