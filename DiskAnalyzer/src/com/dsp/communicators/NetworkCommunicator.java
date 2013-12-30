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
      double tolerance = decimalIntToDouble(word, WORK_TOL_DEC_CASES);
      Log.info("Tolerance: " + tolerance);
      return tolerance / 100.0;
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not check if there's available data.", e);
      throw e;
    }
  }

  
  private void printErrorSegments(List<Segment> seg_info, String area, int address) throws Exception {
    int errors = 0;
    for (Segment s : seg_info) {
      if (s.isBroken()) {
        if (++errors > MAX_ERRORS_DISP) {
          Log.warn("Too many errors!");
          return;
        }

        byte[] index    = FINSFrames.decToHexBytes(s.getIndex() + 1);
        byte[] pos      = FINSFrames.decToHexBytes(s.midPosition());
        byte[] orig_sal = FINSFrames.decToHexBytes(doubleToDInt(s.getOriginalSalience(), DISP_DEC_CASES));
        byte[] orig_wrk = FINSFrames.decToHexBytes(doubleToDInt(s.getOriginalWorkload(), DISP_DEC_CASES));
        byte[] correct  = FINSFrames.decToHexBytes(doubleToDInt(s.correction(), DISP_DEC_CASES));
        byte[] fix_wrk  = FINSFrames.decToHexBytes(doubleToDInt(s.getFixedWorkload(), DISP_DEC_CASES));
        
        byte[] data = {
          index[2],    index[3],
          pos[2],      pos[3],
          orig_sal[2], orig_sal[3],
          orig_wrk[2], orig_wrk[3],
          correct[2],  correct[3],
          fix_wrk[2],  fix_wrk[3],
        };
        
        _client.writeAreaToPLC(data, area, address);
        address += ERROR_DISP_STEP;
      }
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
      
      // Print Errors
      final String F_ERROR_DISP_AREA = "D";
      final int    F_ERROR_DISP_ADDR = 20001;
      final String B_ERROR_DISP_AREA = "D";
      final int    B_ERROR_DISP_ADDR = 20101;
      
      printErrorSegments(disc.getFrontSegment(), F_ERROR_DISP_AREA, F_ERROR_DISP_ADDR);
      printErrorSegments(disc.getBackSegment(),  B_ERROR_DISP_AREA, B_ERROR_DISP_ADDR);
      
      // Print Indexes
      byte[] buffer = new byte[disc.size() * 2];
      
      for (int i = 0; i < buffer.length; i+= 2) {
        byte[] word = FINSFrames.decToHexBytes((i / 2) + 1);
        buffer[i]     = word[2];
        buffer[i + 1] = word[3];
      }
      _client.writeAreaToPLC(buffer, PRINT_AREA, P_INDEX_ADDR);
      
      // Print Mid Positions
      for (int i = 0; i < buffer.length; i+= 2) {
        int pos = disc.getFrontSegment().get(i / 2).midPosition();
        byte[] word = FINSFrames.decToHexBytes(pos);
        buffer[i]     = word[2];
        buffer[i + 1] = word[3];
      }
      _client.writeAreaToPLC(buffer, PRINT_AREA, P_MIDPOS_ADDR);
      
      // Print Front Salience
      for (int i = 0; i < buffer.length; i+= 2) {
        int sal = doubleToDInt(disc.getFrontSegment().get(i / 2).getOriginalSalience(), DISP_DEC_CASES);
        byte[] word = FINSFrames.decToHexBytes(sal);
        buffer[i]     = word[2];
        buffer[i + 1] = word[3];
      }
      _client.writeAreaToPLC(buffer, PRINT_AREA, P_FSAL_ADDR);
      
      // Print Back Salience
      for (int i = 0; i < buffer.length; i+= 2) {
        int sal = doubleToDInt(disc.getBackSegment().get(i / 2).getOriginalSalience(), DISP_DEC_CASES);
        byte[] word = FINSFrames.decToHexBytes(sal);
        buffer[i]     = word[2];
        buffer[i + 1] = word[3];
      }
      _client.writeAreaToPLC(buffer, PRINT_AREA, P_BSAL_ADDR);
      
   // Print Front Work
      for (int i = 0; i < buffer.length; i+= 2) {
        int sal = doubleToDInt(disc.getFrontSegment().get(i / 2).getOriginalWorkload(), DISP_DEC_CASES);
        byte[] word = FINSFrames.decToHexBytes(sal);
        buffer[i]     = word[2];
        buffer[i + 1] = word[3];
      }
      _client.writeAreaToPLC(buffer, PRINT_AREA, P_FWORK_ADDR);
      
      // Print Back Work
      for (int i = 0; i < buffer.length; i+= 2) {
        int sal = doubleToDInt(disc.getBackSegment().get(i / 2).getOriginalWorkload(), DISP_DEC_CASES);
        byte[] word = FINSFrames.decToHexBytes(sal);
        buffer[i]     = word[2];
        buffer[i + 1] = word[3];
      }
      _client.writeAreaToPLC(buffer, PRINT_AREA, P_BWORK_ADDR);
      
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
    return decimalIntToDouble(word, decimal_cases);
  }
  
  /**
   * 
   * @param value
   * @param dec_cases
   * @return
   */
  private int doubleToDInt(double value, int dec_cases) {
    return (int) Math.round((value * Math.pow(10, dec_cases)));
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

  protected static final String STATE_AREA  = Configurations.getInstance().getProperty("STATE_AREA");
  protected static final int    STATE_ADDR  = Configurations.getInstance().getInt("STATE_ADDR");
  protected static final int    STATE_OK    = Configurations.getInstance().getInt("STATE_OK");
  protected static final int    STATE_NOK   = Configurations.getInstance().getInt("STATE_NOK");
  protected static final int    STATE_LDING = Configurations.getInstance().getInt("STATE_LDING");
  protected static final int    STATE_ERROR = Configurations.getInstance().getInt("STATE_ERROR");
  
  // Print Info
  protected static final int DISP_DEC_CASES   = Configurations.getInstance().getInt("DISP_DEC_CASES");
  
  protected static final String PRINT_AREA    = Configurations.getInstance().getProperty("PRINT_AREA");
  protected static final int    P_INDEX_ADDR  = Configurations.getInstance().getInt("P_INDEX_ADDR");
  protected static final int    P_MIDPOS_ADDR = Configurations.getInstance().getInt("P_MIDPOS_ADDR");
  protected static final int    P_FSAL_ADDR   = Configurations.getInstance().getInt("P_FSAL_ADDR");
  protected static final int    P_BSAL_ADDR   = Configurations.getInstance().getInt("P_BSAL_ADDR");
  protected static final int    P_FWORK_ADDR  = Configurations.getInstance().getInt("P_FWORK_ADDR");
  protected static final int    P_BWORK_ADDR  = Configurations.getInstance().getInt("P_BWORK_ADDR");
  
  // Errors
  protected static final int MAX_ERRORS_DISP  = Configurations.getInstance().getInt("MAX_ERRORS_DISP");
  protected static final int ERROR_DISP_STEP  = Configurations.getInstance().getInt("ERROR_DISP_STEP");
}
