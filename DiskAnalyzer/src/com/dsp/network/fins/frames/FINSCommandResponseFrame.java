package com.dsp.network.fins.frames;


public class FINSCommandResponseFrame extends FINSFrame {

  public static final byte[] TEMPLATE = {
    // Command
    (byte) 0x00, (byte) 0x00,
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
  
  public byte[] getDataBuffer() { 
    return _data; 
  }
  
  public byte getEndCode() { 
    return _frame[ENDC]; 
  }
  
  public int dataLength() {
    return _data.length;
  }
  
  public boolean hasError() {
    return _frame[ENDC] != (byte) 0x00;
  }
  
  public static int frameLength() {
    return TEMPLATE.length;
  }
  
  /**
   * Error messages taken from the W421 manual (Section 7-4)
   * @return the error message corresponding to the error code
   */
  public String getErrorMessage() throws Exception {
    switch (_frame[ENDC]) {
    case 0x00: return "No problem exists.";
    case 0x01: return "The command that was sent cannot be executed when the PLC is in RUN mode.";
    case 0x02: return "The command that was sent cannot be executed when the PLC is in MONITOR mode.";
    case 0x03: return "The PLC's UM is write-protected.";
    case 0x04: return "The program address setting in an read or write command is above the highest program address.";
    case 0x0B: return "The command that was sent cannot be executed when the PLC is in PROGRAM mode.";
    case 0x13: return "The FCS is wrong.";
    case 0x14: return "The command format is wrong, or a command that cannot be divided has been divided, or the frame length is smaller than the minimum length for the applicable command.";
    case 0x15: return "The data is outside of the specified range or too long. / Hexadecimal data has not been specified.";
    case 0x16: return "The operand specified in an SV Read or SV Change command does not exist in the program.";
    case 0x18: return "The maximum frame length of 131 bytes was exceeded.";
    case 0x19: return "The read SV exceeded 9,999, or an I/O memory batch read was exe- cuted when items to read were not registered for composite command, or access right was not obtained.";
    case 0x20: return "Unrecognized Remote I/O Unit, too many I/O words, or word duplication used.";
    case 0x21: return "The command cannot be executed because a CPU error has occurred in the CPU Unit.";
    case 0x23: return "The UM is read-protected or write- protected.";
    case (byte) 0xA3: return "An FCS error occurred in the second or later frame, or there were two bytes or less of data in an intermediate or final frame for multiple writing.";
    case (byte) 0xA4: return "The command format did not match the number of bytes in the second or later frame.";
    case (byte) 0xA5: return "There was an entry number data error in the second or later frame, a data length error, or data was not set in hexadecimal.";
    case (byte) 0xA8: return "The length of the second and later frames exceeded the maximum of 128 bytes.";
    default:   throw new Exception("Unkown error code found in the frame.");
    }
  }
 
  // fields indexes
  private static final int ENDC = 3;

}
