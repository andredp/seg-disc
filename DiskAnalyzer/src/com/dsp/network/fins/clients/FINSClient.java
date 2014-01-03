package com.dsp.network.fins.clients;

import java.util.ArrayList;

import com.dsp.config.Configurations;
import com.dsp.network.exceptions.TooManyBadPacketsException;
import com.dsp.network.fins.frames.FINSCommandResponseFrame;

public abstract class FINSClient {
  
  protected static final int MAXBADPACKETS  = Configurations.getInstance().getInt("PLC_MAXBADPACKETS");
  protected static final int READCHUNK      = Configurations.getInstance().getInt("PLC_READCHUNK");
  protected static final int WRITECHUNK     = Configurations.getInstance().getInt("PLC_WRITECHUNK");
  protected static final int BYTES_PER_WORD = Configurations.getInstance().getInt("BYTES_PER_WORD");
  
  public abstract void connect()    throws Exception;
  public abstract void disconnect() throws Exception;
  
  protected abstract FINSCommandResponseFrame sendCommand(String type, String area, int address, int words)              throws Exception;
  protected abstract FINSCommandResponseFrame sendCommand(String type, String area, int address, int words, byte[] data) throws Exception;
  
  /**
   * 
   * @param area
   * @param address
   * @param numWords
   * @return
   * @throws Exception
   */
  public ArrayList<Byte> readAreaFromPLC(String area, int address, int numWords) throws Exception {
    int read   = 0;
    int errors = 0;
    ArrayList<Byte> data = new ArrayList<Byte>();
    
    while (read < numWords) {
      int toread = Math.min(READCHUNK, numWords - read);
      FINSCommandResponseFrame response = sendCommand("area_read", area, address + read, toread);
      if (!response.hasError()) {
        // adds the words to the data buffer
        for (byte b : response.getDataBuffer()) {
          data.add(b);
        }
        read += toread;
        errors = 0;
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
    int word = readWordFromPLC(area, offset);
    byte read = (byte) (word & (0x01 << bit) >> bit);
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
  
  /**
   * 
   * @param data
   * @param area
   * @param address
   * @throws Exception
   */
  public void writeAreaToPLC(byte[] data, String area, int address) throws Exception {
    int written = 0; // words
    int errors  = 0;
    
    final int total = data.length / BYTES_PER_WORD;
    while (written < total) {
      int towrite = Math.min(WRITECHUNK, total - written);
      
      byte[] dataToSend = new byte[towrite * BYTES_PER_WORD];
      for (int i = 0; i < dataToSend.length; i++) {
        dataToSend[i] = data[(written * BYTES_PER_WORD) + i];
      }
      
      FINSCommandResponseFrame response = sendCommand("area_write", area, address + written, towrite, dataToSend);
      if (!response.hasError()) {
        written += towrite;
        errors = 0;
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
