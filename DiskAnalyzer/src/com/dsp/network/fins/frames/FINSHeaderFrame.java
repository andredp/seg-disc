package com.dsp.network.fins.frames;

public class FINSHeaderFrame extends Frame {

  private static final byte[] TEMPLATE = {
    (byte) 0x80,    // ICF - 0x00 = Command that requires a response
    (byte) 0x00,    // RSV - Reserved (always 0x00)
    (byte) 0x02,    // GCT - Max number of gateways to pass through
    (byte) 0x00,    // DNA - 0x00 = Local Network (Destination Network Address)
    (byte) 0x00,    // DA1 - 0x00 = Local PLC Node
    (byte) 0x00,    // DA2 - 0x00 = CPU Unit
    (byte) 0x00,    // SNA - 0x00 = Local Network (Source Network Address)
    (byte) 0x00,    // SA1 - PC Node (Last parameter of this machine's IP)
    (byte) 0x00,    // SA2 - 0x00 = CPU Unit
    (byte) 0x00     // SID - A random number (Service ID)
  };
  
  public FINSHeaderFrame() {
    super(TEMPLATE.clone());
  }
  
  public FINSHeaderFrame(byte client_node, byte server_node) {
    super(TEMPLATE.clone());
    _frame[DA1] = server_node;
    _frame[SA1] = client_node;
  }

  public byte getSID()         { return _frame[SID]; }
  public byte getDA1()         { return _frame[DA1]; }
  public byte getSA1()         { return _frame[SA1]; }

  public void setSID(byte sid) { _frame[SID] = sid;  }
  public void setDA1(byte da1) { _frame[DA1] = da1;  }
  public void setSA1(byte sa1) { _frame[SA1] = sa1;  }
  
  // the same as above but more expressive
  public byte getServerNode()        { return _frame[DA1]; }
  public byte getClientNode()        { return _frame[SA1]; }
  
  public void setServerNode(byte cn) { _frame[DA1] = cn; }
  public void setClientNode(byte cn) { _frame[SA1] = cn; }

  public static int frameLength() {
    return TEMPLATE.length;
  }
  
  // fields indexes
  private static final int DA1 = 4;
  private static final int SA1 = 7;
  private static final int SID = 9;

}
