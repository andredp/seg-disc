package com.dsp.receivers;

import java.util.ArrayList;

import com.dsp.analyzer.DiscRawData;
import com.dsp.config.Configs;
import com.dsp.libs.Utils;
import com.dsp.network.fins.clients.FINSClient;
import com.dsp.network.fins.clients.FINSTCPClient;
import com.esotericsoftware.minlog.Log;

public class NetworkReceiver implements Receiver {
  
  FINSClient _client = null;
  
  /**
   * 
   * @param client
   */
  public NetworkReceiver(FINSClient client) {
    _client = client;
  }
  
  /**
   * 
   * @throws Exception
   */
  public NetworkReceiver() throws Exception {
    try {
      _client = new FINSTCPClient(PLC_IP, PLC_PORT);
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not receive the data.", e);
      throw e;
    }
  }
  
  /**
   * 
   */
  @Override
  public boolean hasData() throws Exception {
    try {
      return _client.readBitFromPLC(RUN_BIT_AREA, RUN_BIT_ADDR, RUN_BIT_OFFSET);
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not check if there's available data.", e);
      throw e;
    }
  }
  
  /**
   * 
   */
  @Override
  public void waitForData() throws Exception {
    try {
      while (true) {
        if (hasData()) return;
        Thread.sleep(CHECK_INTERVAL);
      }
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not check if there's available data.", e);
      throw e;
    }
  }
  
  /**
   * 
   */
  @Override
  public double getWorkTolerance() throws Exception {
    try {
      int word = _client.readWordFromPLC(WORK_TOL_AREA, WORK_TOL_ADDR);
      return Utils.decimalIntToDouble(word, WORK_TOL_DEC_CASES) / 100.0;
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not check if there's available data.", e);
      throw e;
    }
  }
  
  /**
   * 
   */
  @Override
  public DiscRawData receive() throws Exception {
    try {
      int fwords = FRONT_ADDR[1] - FRONT_ADDR[0];
      int bwords = BACK_ADDR[1]  - BACK_ADDR[0];
      ArrayList<Byte> frontBytes = _client.readAreaFromPLC(SEG_DATA_AREA, FRONT_ADDR[0], fwords);
      ArrayList<Byte> backBytes  = _client.readAreaFromPLC(SEG_DATA_AREA, BACK_ADDR[0],  bwords);
      
      return new DiscRawData(parseRawBytes(frontBytes), parseRawBytes(backBytes));
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not receive the data.", e);
      throw e;
    }
    
    /* TO TEST
    DummyReceiver comm = new DummyReceiver();
    Thread.sleep(2000);
    return comm.receive();
    */
  }

  @Override
  public void signalWorkComplete() throws Exception {
    try {
      _client.writeBitToPLC(false, RUN_BIT_AREA, RUN_BIT_ADDR, RUN_BIT_OFFSET);
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not check if there's available data.", e);
      throw e;
    }
  }

  /**
   * 
   */
  @Override
  public void disconnect() {
    try {
      _client.disconnect();
    } catch (Exception e) {
      Log.warn("NetworkReceiver", "Exception thrown while disconnecting.", e);
    }
  }
  
  /**
   * 
   * @param rawBytes
   * @return
   */
  private double[] parseRawBytes(ArrayList<Byte> rawBytes) {
    double[] segData = new double[rawBytes.size() / BYTES_PER_WORD];
    for (int i = 0; i < segData.length; i++) {
      byte highPart = rawBytes.get( i * BYTES_PER_WORD);
      byte lowPart  = rawBytes.get((i * BYTES_PER_WORD) + 1);
      segData[i] = Utils.dIntToDouble(highPart, lowPart, SEG_DATA_DEC_CASES);
    }
    return segData;
  }
  
  
  // ====== CONSTANTS
  
  private static final int    BYTES_PER_WORD = Configs.getInstance().getInt("BYTES_PER_WORD");
  
  private static final String PLC_IP   = Configs.getInstance().getProperty("PLC_IP");
  private static final int    PLC_PORT = Configs.getInstance().getInt("PLC_PORT");

  private static final String SEG_DATA_AREA      = Configs.getInstance().getProperty("SEG_DATA_AREA");
  private static final int[]  FRONT_ADDR         = Configs.getInstance().getVector2i("FRONT_SEG_DATA");
  private static final int[]  BACK_ADDR          = Configs.getInstance().getVector2i("BACK_SEG_DATA");
  private static final int    SEG_DATA_DEC_CASES = Configs.getInstance().getInt("SEG_DATA_DEC_CASES");
  
  private static final String RUN_BIT_AREA   = Configs.getInstance().getProperty("RUN_BIT_AREA");
  private static final int    RUN_BIT_ADDR   = Configs.getInstance().getInt("RUN_BIT_ADDR");
  private static final int    RUN_BIT_OFFSET = Configs.getInstance().getInt("RUN_BIT_OFFSET");
  private static final int    CHECK_INTERVAL = Configs.getInstance().getInt("RUN_CHECK_INTERVAL");
  
  private static final String WORK_TOL_AREA      = Configs.getInstance().getProperty("WORK_TOL_AREA");
  private static final int    WORK_TOL_ADDR      = Configs.getInstance().getInt("WORK_TOL_ADDR");
  private static final int    WORK_TOL_DEC_CASES = Configs.getInstance().getInt("WORK_TOL_DEC_CASES");
}
