package com.dsp.communicators;

import java.util.ArrayList;

import com.dsp.analyzer.DiscRawData;
import com.dsp.analyzer.SegmentedDisc;
import com.dsp.analyzer.config.Configurations;
import com.dsp.communicators.fins.FINSClient;
import com.dsp.communicators.fins.FINS_TCPClient;
import com.esotericsoftware.minlog.Log;

public class NetworkCommunicator implements Communicator {
  
  /**
   * 
   */
  @Override
  public DiscRawData receive() throws Exception {
    FINSClient client = null;
    try {
      client = new FINS_TCPClient(PLC_IP, PLC_PORT);
      client.connect();
      
      int fwords = FRONT_ADDR[1] - FRONT_ADDR[0];
      int bwords = BACK_ADDR[1] - BACK_ADDR[0];
      ArrayList<Byte> frontBytes = client.readAreaFromPLC(SEG_DATA_AREA, FRONT_ADDR[0], fwords);
      ArrayList<Byte> backBytes  = client.readAreaFromPLC(SEG_DATA_AREA, BACK_ADDR[0],  bwords);
      
      return new DiscRawData(parseRawBytes(frontBytes), parseRawBytes(backBytes));
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not receive the data.", e);
      throw e;
    } finally {
      if (client != null) {
        client.disconnect();
      }
    }
  }
  
  
  @Override
  public boolean hasData() throws Exception {
    FINSClient client = null;
    try {
      client = new FINS_TCPClient(PLC_IP, PLC_PORT);
      client.connect();
      while (true) {
        boolean run_bit = client.readBitFromPLC(RUN_BIT_AREA, RUN_BIT_ADDR, RUN_BIT_OFFSET);
        if (run_bit == true) {
          return true;
        }
        Thread.sleep(CHECK_INTERVAL);
      }
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not check if there's available data.", e);
      throw e;
    } finally {
      if (client != null) {
        client.disconnect();
      }
    }
  }
  
  
  /**
   * 
   */
  @Override
  public void notifyLoading() throws Exception {
    /*FINSClient client = null;
    try {
      client = new FINS_TCPClient(PLC_IP, PLC_PORT);
      client.connect();
    //  client.writeBitToPLC(false, , address, bit)
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not check if there's available data.", e);
      throw e;
    } finally {
      if (client != null) {
        client.disconnect();
      }
    }*/
  }
  
  @Override
  public void printResult(SegmentedDisc disc) throws Exception {
    // TODO
  }
  
  
  @Override
  public void workDone() throws Exception {
    FINSClient client = null;
    try {
      client = new FINS_TCPClient(PLC_IP, PLC_PORT);
      client.connect();
      client.writeBitToPLC(false, RUN_BIT_AREA, RUN_BIT_ADDR, RUN_BIT_OFFSET);
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not check if there's available data.", e);
      throw e;
    } finally {
      if (client != null) {
        client.disconnect();
      }
    }
  }

  
  /**
   * 
   */
  @Override
  public double getWorkTolerance() throws Exception {
    FINSClient client = null;
    try {
      client = new FINS_TCPClient(PLC_IP, PLC_PORT);
      client.connect();
      int word = client.readWordFromPLC(WORK_TOL_AREA, WORK_TOL_ADDR);
      return decimalIntToDouble(word, WORK_TOL_DEC_CASES);
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not check if there's available data.", e);
      throw e;
    } finally {
      if (client != null) {
        client.disconnect();
      }
    }
  }
  
  /**
   * 
   * @param rawBytes
   * @return
   */
  private double[] parseRawBytes(ArrayList<Byte> rawBytes) {
    double[] segData = new double[rawBytes.size() / 2];
    for (int i = 0; i < segData.length; i++) {
      segData[i] = decimalIntToDouble(rawBytes.get(i * 2), rawBytes.get((i * 2) + 1), SEG_DATA_DEC_CASES);
    }
    return segData;
  }

  /**
   * 
   * @param high_part
   * @param low_part
   * @return
   */
  private double decimalIntToDouble(byte high_part, byte low_part, int decimal_cases) {
    int word = (((int)high_part & 0xff) << 8) | ((int)low_part & 0xff);
    return decimalIntToDouble(word, decimal_cases);
  }
  
  /**
   * 
   * @param word
   * @return
   */
  private double decimalIntToDouble(int word, int decimal_cases) {
    return (double) word / Math.pow(10, decimal_cases);
  }
  
  
  private static final String PLC_IP   = Configurations.getInstance().getProperty("PLC_IP");
  private static final int    PLC_PORT = Configurations.getInstance().getInt("PLC_PORT");

  protected static final int SEG_DATA_DEC_CASES = Configurations.getInstance().getInt("SEG_DATA_DEC_CASES");
  protected static final int WORK_TOL_DEC_CASES = Configurations.getInstance().getInt("WORK_TOL_DEC_CASES");
  
  protected static final int CHECK_INTERVAL = Configurations.getInstance().getInt("RUN_CHECK_INTERVAL");
  
  protected static final String SEG_DATA_AREA = Configurations.getInstance().getProperty("SEG_DATA_AREA");
  protected static final int[]  FRONT_ADDR    = Configurations.getInstance().getVector2i("FRONT_SEG_DATA");
  protected static final int[]  BACK_ADDR     = Configurations.getInstance().getVector2i("BACK_SEG_DATA");
  
  protected static final String RUN_BIT_AREA   = Configurations.getInstance().getProperty("RUN_BIT_AREA");
  protected static final int    RUN_BIT_ADDR   = Configurations.getInstance().getInt("RUN_BIT_ADDR");
  protected static final int    RUN_BIT_OFFSET = Configurations.getInstance().getInt("RUN_BIT_OFFSET");

  protected static final String WORK_TOL_AREA  = Configurations.getInstance().getProperty("WORK_TOL_AREA");
  protected static final int    WORK_TOL_ADDR  = Configurations.getInstance().getInt("WORK_TOL_ADDR");

}
