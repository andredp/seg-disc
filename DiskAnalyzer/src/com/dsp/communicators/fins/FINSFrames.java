package com.dsp.communicators.fins;

import com.dsp.analyzer.config.Configurations;
import com.dsp.communicators.exceptions.UnknownCommandTypeException;
import com.dsp.communicators.exceptions.UnknownMemoryAreaException;

public class FINSFrames {

  public static final int BYTES_PER_WORD = Configurations.getInstance().getInt("BYTES_PER_WORD");
  
//==================================================================================
  // == FINS HEADER FRAME ==
  public static final byte[] FINS_HEADER = {
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
  
  // FINS Header fields indexes
  public static final int FH_ICF = 0;
  public static final int FH_RSV = 1;
  public static final int FH_GCT = 2;
  public static final int FH_DNA = 3;
  public static final int FH_DA1 = 4;
  public static final int FH_DA2 = 5;
  public static final int FH_SNA = 6;
  public static final int FH_SA1 = 7;
  public static final int FH_SA2 = 8;
  public static final int FH_SID = 9;
  
  /**
   * 
   * @param client_node
   * @param server_node
   * @return
   */
  public static byte[] createFinsHeaderFrame(byte client_node, byte server_node, int serviceID) {
    byte[] header  = FINS_HEADER.clone();
    
    header[FH_DA1] = server_node;
    header[FH_SA1] = client_node;
    header[FH_SID] = (byte) serviceID;
    return header;
  }
  
  
//==================================================================================
  // == FINS COMMAND FRAME ==
  public static final byte[] FINS_COMMAND = {
    // Command 
    (byte) 0x00, (byte) 0x00, // Command (Area Read Command 0101)
    // Parameters
    (byte) 0x00,              // Memory area designation
    (byte) 0x00, (byte) 0x00, //(byte) 0x00, // Beginning address
    (byte) 0x00,              // Beginning bit (0x00 if a word is to be read)
    (byte) 0x00, (byte) 0x00  // Amount to read (999 is the max in ethernet)
  };

  // FINS Command fields indexes
  public static final int FC_COM_1  = 0;
  public static final int FC_COM_2  = 1;
  public static final int FC_MEM    = 2;
  public static final int FC_ADDR_1 = 3; 
  public static final int FC_ADDR_2 = 4;
  public static final int FC_BIT    = 5;
//  public static final int FC_ADDR_3 = 5;
  public static final int FC_NUM_1  = 6; 
  public static final int FC_NUM_2  = 7;
  
  /**
   * 
   * @param area
   * @param address
   * @param amount
   * @return
   * @throws UnknownMemoryAreaException
   */
  public static byte[] createCommandFrame(String type, String area, int address, int amount) 
      throws UnknownMemoryAreaException, UnknownCommandTypeException {
    // Clones the command template
    byte[] command = FINS_COMMAND.clone();
    
    // Command type 
    byte[] command_code = getCommandCode(type);
    command[FC_COM_1]   = command_code[0];
    command[FC_COM_2]   = command_code[1];
    // Area Code
    command[FC_MEM]     = getAreaCode(area);
    // Starting address in that area
    byte[] addressBytes = decToHexBytes(address);
    command[FC_ADDR_1]  = addressBytes[2];
    command[FC_ADDR_2]  = addressBytes[3];
   // command[FC_ADDR_3]  = addressBytes[3];
    // Number of words to read
    byte[] num_words    = decToHexBytes(amount);
    command[FC_NUM_1]   = num_words[2];
    command[FC_NUM_2]   = num_words[3];

    return command;
  }

  
//==================================================================================
  // == FINS RESPONSE FRAME ==
  public static final byte[] FINS_RESPONSE = {
    // Command
    (byte) 0x01, (byte) 0x01,
    // End Code
    (byte) 0x00, (byte) 0x00, // 0x0000 means everything was OK
    // Data
    (byte) 0x00, (byte) 0x00 // ... each word has 2 bytes (PC1L)
    // ...
  };
  
  // FINS Response fields indexes
  public static final int FR_COM_1 = 0;
  public static final int FR_COM_2 = 1;
  public static final int FR_END_1 = 2;
  public static final int FR_END_2 = 3;
  
  public static final int FR_HEADER_SIZE = 4;
  
  /**
   * 
   * @param response
   * @return
   */
  public static boolean responseFrameOK(byte[] response) {
    return response[FR_END_1] == (byte) 0x00 && response[FR_END_2] == (byte) 0x00;
  }
  
  
  
  // ==================================================================================
  // == FINS TCP SEND FRAME ==
  public static byte[] FINS_TCP_SEND_FRAME = new byte[] {
    (byte) 'F',  (byte) 'I',  (byte) 'N',  (byte) 'S',
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Length
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Command
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00  // Error Code (Always 0x00)
  };
  
  public static final int FTF_LENG = 4;
  public static final int FTF_COMM = 8;
  public static final int FTF_ERR  = 12;
  
  public static final int FTF_DATALENGTH = 8; // Length from command to the end
  public static final int SEND_FRAME_LENGTH = FTF_DATALENGTH + FINS_HEADER.length +
                                              FINS_COMMAND.length;
  
  /**
   * 
   * @param length
   * @return
   */
  public static byte[] createTCPSendFrame(int length) {
    byte[] tcp_header = FINS_TCP_SEND_FRAME.clone();
    
    byte[] hex_length = decToHexBytes(length);
    tcp_header[FTF_LENG]     = hex_length[0];
    tcp_header[FTF_LENG + 1] = hex_length[1];
    tcp_header[FTF_LENG + 2] = hex_length[2];
    tcp_header[FTF_LENG + 3] = hex_length[3];
    return tcp_header;
  }
  
  // == FINS TCP CONNECTION FRAME ==
  public static byte[] FINS_TCP_FRAME = new byte[] {
    (byte) 'F',  (byte) 'I',  (byte) 'N',  (byte) 'S',
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C, // Length
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Command
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Error Code (Always 0x00)
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Client node (0 = automatic)
  };
  
  public static final int FTF_CLIN = 16;
  public static final int FTF_SRVN = 20;
  
  /**
   * 
   * @param client_node
   * @return
   */
  public static byte[] createTCPConnectFrame(int client_node) {
    byte[] tcp_header = FINS_TCP_FRAME.clone();
    
    tcp_header[FTF_CLIN] = (byte) client_node;
    return tcp_header;
  }

  // ==================================================================================

  
  /**
   * 
   * @param decimal
   * @return
   */
  public static byte[] decToHexBytes(int decimal) {
    return new byte[] {
      (byte) (((decimal & 0xff000000) >> 24) & 0x000000ff),
      (byte) (((decimal & 0x00ff0000) >> 16) & 0x000000ff),
      (byte) (((decimal & 0x0000ff00) >>  8) & 0x000000ff),
      (byte)   (decimal & 0x000000ff)
    };
  }
  
  
  /**
   * 
   * @param area
   * @return
   * @throws UnknownMemoryAreaException
   */
  public static byte getAreaCode(String area) throws UnknownMemoryAreaException {
    if (area.equalsIgnoreCase("D")) { return (byte) 0x82; }
    // enter other areas...
    throw new UnknownMemoryAreaException(area);
  }
  
  
  /**
   * 
   * @param type
   * @return
   * @throws UnknownCommandTypeException
   */
  public static byte[] getCommandCode(String type) throws UnknownCommandTypeException {
    if (type.equalsIgnoreCase("area_read"))  { return new byte[] { (byte) 0x01, (byte) 0x01 }; }
    if (type.equalsIgnoreCase("area_write")) { return new byte[] { (byte) 0x01, (byte) 0x02 }; }
    // enter other commands...
    throw new UnknownCommandTypeException(type);
  }
  
  public static int tcpHeaderErrorCode(byte[] tcp_header) {
    return tcp_header[FTF_ERR + 2] << 8 | tcp_header[FTF_ERR + 3];
  }
}
