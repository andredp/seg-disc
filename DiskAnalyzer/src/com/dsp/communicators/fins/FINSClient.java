package com.dsp.communicators.fins;

import java.util.ArrayList;

import com.dsp.analyzer.config.Configurations;
import com.dsp.communicators.exceptions.TooManyBadPacketsException;

public abstract class FINSClient {
  
  protected static final int MAXBADPACKETS = Configurations.getInstance().getInt("PLC_MAXBADPACKETS");
  protected static final int READCHUNK     = Configurations.getInstance().getInt("PLC_READCHUNK");
  protected static final int WRITECHUNK    = Configurations.getInstance().getInt("PLC_WRITECHUNK");
  
  public abstract void connect()    throws Exception;
  public abstract void disconnect() throws Exception;
  
  protected abstract byte[] getReadResponseFrame(String area, int address, int words) throws Exception;
  protected abstract byte[] getWriteResponseFrame(byte[] data, String area, int address) throws Exception;
  
  // Reads
  /**
   * 
   * @param area
   * @param offset
   * @param numWords
   * @return
   * @throws Exception
   */
  public ArrayList<Byte> readAreaFromPLC(String area, int offset, int numWords) throws Exception {
    int read   = 0;
    int errors = 0;
    ArrayList<Byte> data = new ArrayList<Byte>();
    
    while (read < numWords) {
      int toread = Math.min(READCHUNK, numWords - read);
      
      byte[] response = getReadResponseFrame(area, offset + read, toread);
      
      // Data parsing
      if (FINSFrames.responseFrameOK(response)) {
        // adds the words to the data buffer
        for (int i = FINSFrames.FR_HEADER_SIZE; i < response.length; i++) {
          data.add(response[i]);
        }
        read += toread;
        errors = 0;
        // In case the end code is not OK, tries again until a certain number of tries
      } else if (errors++ >= MAXBADPACKETS) {
        throw new TooManyBadPacketsException();
      }
    }
    
    return data;
  }
  
  /**
   * 
   * @param area
   * @param offset
   * @param bit
   * @return
   * @throws Exception
   */
  public boolean readBitFromPLC(String area, int offset, int bit)  throws Exception {
    byte read = (byte) (readWordFromPLC(area, offset) & (0x01 << bit) >> bit);
    return (read == 0x01 ? true : false);
  }
  
  /**
   * 
   * @param area
   * @param address
   * @return
   * @throws Exception
   */
  public int readWordFromPLC(String area, int address) throws Exception {
    ArrayList<Byte> data = readAreaFromPLC(area, address, 1);
    return ((int) data.get(0) & 0xff) << 8 | ((int) data.get(1) & 0xff);
  }
  
  // Writes
  public void writeAreaToPLC(byte[] data, String area, int address) throws Exception {
    final int BYTES_PER_WORD = 2;
    int written = 0; // words
    int errors  = 0;
    
    final int total = data.length / BYTES_PER_WORD;
    while (written < total) {
      int towrite = Math.min(WRITECHUNK, total - written);
      
      byte[] data_to_write = new byte[towrite * BYTES_PER_WORD];
      for (int i = 0; i < data_to_write.length; i++) {
        data_to_write[i] = data[(written * BYTES_PER_WORD) + i];
      }
      
      byte[] response = getWriteResponseFrame(data_to_write, area, address + written);
      
      // Data parsing
      if (FINSFrames.responseFrameOK(response)) {
        written += towrite;
        errors = 0;
        // In case the end code is not OK, tries again until a certain number of tries
      } else if (errors++ >= MAXBADPACKETS) {
        throw new TooManyBadPacketsException();
      }
    }
  }
  
  /**
   * 
   * @param data
   * @param area
   * @param address
   * @return
   * @throws Exception
   */
  public void writeWordToPLC(int data, String area, int address) throws Exception {
    byte[] word = {
      (byte) ((data & 0xff00) >> 8),
      (byte)  (data & 0x00ff)
    };
    writeAreaToPLC(word, area, address);
  }
  
  /**
   * 
   * @param data
   * @param area
   * @param address
   * @param bit
   * @return
   * @throws Exception
   */
  public void writeBitToPLC(boolean data, String area, int address, int bit) throws Exception {
    int word = readWordFromPLC(area, address);
    if (data == true) {
      word |= 0x01 << bit;
    } else {
      word &= ~(0x01 << bit);
    }
    writeWordToPLC(word, area, address);
  }

}
