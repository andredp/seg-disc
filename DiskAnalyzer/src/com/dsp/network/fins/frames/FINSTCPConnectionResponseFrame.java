package com.dsp.network.fins.frames;

public class FINSTCPConnectionResponseFrame extends Frame {

  private static final byte[] TEMPLATE = new byte[] {
    (byte) 'F',  (byte) 'I',  (byte) 'N',  (byte) 'S',
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x10, // Length
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Command
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Error Code
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Client node (0x01 to 0xFE)
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Server node (0x01 to 0xFE)
  };
  
  public FINSTCPConnectionResponseFrame() {
    super(TEMPLATE.clone());
  }
  
  public byte getClientNode() { return _frame[CLIN]; }
  public byte getServerNode() { return _frame[SRVN]; }
  public byte getErrorCode()  { return _frame[ERRC]; }
  
  public boolean hasError() {
    return (_frame[COMM] != (byte) 0x01 || _frame[ERRC] != (byte) 0x00);
  }
  
  public static int frameLength() {
    return TEMPLATE.length;
  }
  
  /**
   * Error messages taken from the W421 manual (Section 7-4)
   * @return the error message corresponding to the error code
   */
  public String getErrorMessage() throws Exception {
    switch (_frame[ERRC]) {
    case 0x00: return "Normal (no error).";
    case 0x01: return "The header is not 'FINS' (ASCII code).";
    case 0x02: return "The data length is too long.";
    case 0x03: return "The command is not supported.";
    case 0x20: return "All connections are in use.";
    case 0x21: return "The specified node is already connected.";
    case 0x22: return "Attempt to access a protected node from an unspecified IP address.";
    case 0x23: return "The client FINS node address is out of range.";
    case 0x24: return "The same FINS node address is being used by the client and server.";
    case 0x25: return "All the node addresses available for allocation have been used.";
    default:   throw new Exception("Unkown error code found in the frame.");
    }
  }

  // frame indexes
  private static final int COMM = 11;
  private static final int ERRC = 15;
  private static final int CLIN = 19;
  private static final int SRVN = 23;
  
}
