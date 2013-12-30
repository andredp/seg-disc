package com.dsp.communicators;

import java.util.ArrayList;
import java.util.List;

import com.dsp.analyzer.DiscRawData;
import com.dsp.analyzer.Segment;
import com.dsp.analyzer.SegmentedDisc;
import com.dsp.analyzer.config.Configurations;
import com.dsp.communicators.fins.FINSClient;
import com.dsp.communicators.fins.FINSFrames;
import com.dsp.communicators.fins.FINS_TCPClient;
import com.dsp.libs.Utils;
import com.dsp.renderers.DiscRenderer;
import com.dsp.renderers.NBConsoleRenderer;
import com.esotericsoftware.minlog.Log;

public class NetworkCommunicator implements Communicator {
  
  FINSClient _client = null;
  
  public NetworkCommunicator() throws Exception {
    try {
      _client = new FINS_TCPClient(PLC_IP, PLC_PORT);
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not receive the data.", e);
      throw e;
    }
  }
  
  @Override
  public void disconnect() throws Exception {
    _client.disconnect();
  }
  
  
  /**
   * 
   */
  @Override
  public DiscRawData receive() throws Exception {
    DummyCommunicator comm = new DummyCommunicator();
    Thread.sleep(2000);
    return comm.receive();
    /* TODO CORRIGIRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR
    try {
      _client.testOrConnect();
      
      int fwords = FRONT_ADDR[1] - FRONT_ADDR[0];
      int bwords = BACK_ADDR[1]  - BACK_ADDR[0];
      ArrayList<Byte> frontBytes = _client.readAreaFromPLC(SEG_DATA_AREA, FRONT_ADDR[0], fwords);
      ArrayList<Byte> backBytes  = _client.readAreaFromPLC(SEG_DATA_AREA, BACK_ADDR[0],  bwords);
      
      return new DiscRawData(parseRawBytes(frontBytes), parseRawBytes(backBytes));
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not receive the data.", e);
      throw e;
    }*/
  }
  
  /**
   * 
   */
  @Override
  public boolean hasData() throws Exception {
    try {
      _client.testOrConnect();
      while (true) {
        boolean run_bit = _client.readBitFromPLC(RUN_BIT_AREA, RUN_BIT_ADDR, RUN_BIT_OFFSET);
        if (run_bit == true) {
          return true;
        }
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
  public void notifyLoading() throws Exception {
    try {
      _client.testOrConnect();
      _client.writeWordToPLC(STATE_LDING, STATE_AREA, STATE_ADDR);
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not check if there's available data.", e);
      throw e;
    }
  }
  
  
  @Override
  public void workDone() throws Exception {
    try {
      _client.testOrConnect();
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
  public double getWorkTolerance() throws Exception {
    try {
      _client.testOrConnect();
      int word = _client.readWordFromPLC(WORK_TOL_AREA, WORK_TOL_ADDR);
      double tolerance = Utils.decimalIntToDouble(word, WORK_TOL_DEC_CASES);
      Log.info("Tolerance: " + tolerance);
      return tolerance / 100.0;
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not check if there's available data.", e);
      throw e;
    }
  }

  
  @Override
  public void printResult(SegmentedDisc disc) throws Exception {
    try {
      _client.testOrConnect();
      
      // Print Reading Status
      if (disc.readingError()) {
        _client.writeWordToPLC(STATE_ERROR, STATE_AREA, STATE_ADDR);
        return;
      }
      
      // Print Disc Status
      int state = (disc.isBrokenDisc() ? STATE_NOK : STATE_OK);
      _client.writeWordToPLC(state, STATE_AREA, STATE_ADDR);
      
      // Render data to the NB Console
      DiscRenderer nbconsole = new NBConsoleRenderer(_client);
      nbconsole.render(disc);
      
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not check if there's available data.", e);
      throw e;
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
      segData[i] = dIntToDouble(rawBytes.get(i * 2), rawBytes.get((i * 2) + 1), SEG_DATA_DEC_CASES);
    }
    return segData;
  }

  /**
   * 
   * @param high_part
   * @param low_part
   * @return
   */
  private double dIntToDouble(byte high_part, byte low_part, int decimal_cases) {
    int word = (((int)high_part & 0xff) << 8) | ((int)low_part & 0xff);
    return Utils.decimalIntToDouble(word, decimal_cases);
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

  protected static final String STATE_AREA  = Configurations.getInstance().getProperty("STATE_AREA");
  protected static final int    STATE_ADDR  = Configurations.getInstance().getInt("STATE_ADDR");
  protected static final int    STATE_OK    = Configurations.getInstance().getInt("STATE_OK");
  protected static final int    STATE_NOK   = Configurations.getInstance().getInt("STATE_NOK");
  protected static final int    STATE_LDING = Configurations.getInstance().getInt("STATE_LDING");
  protected static final int    STATE_ERROR = Configurations.getInstance().getInt("STATE_ERROR");

}
