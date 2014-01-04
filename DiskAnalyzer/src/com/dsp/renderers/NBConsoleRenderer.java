package com.dsp.renderers;

import java.util.List;

import com.dsp.analyzer.Segment;
import com.dsp.analyzer.SegmentedDisc;
import com.dsp.config.Configs;
import com.dsp.libs.Utils;
import com.dsp.network.fins.clients.FINSClient;
import com.dsp.network.fins.clients.FINSTCPClient;
import com.esotericsoftware.minlog.Log;

public class NBConsoleRenderer implements DiscRenderer {
  
  private FINSClient _client; 

  public NBConsoleRenderer(FINSClient client) {
    _client = client;
  }
  
  public NBConsoleRenderer() throws Exception {
    try {
      _client = new FINSTCPClient(PLC_IP, PLC_PORT);
      _client.connect();
    } catch (Exception e) {
      Log.error("NBConsoleRenderer", "Could not initialize the client.", e);
      throw e;
    }
  }
  
  /**
   * 
   */
  @Override
  public void render(SegmentedDisc disc) throws Exception {
    try {
      // Print Reading Status
      if (disc.readingError()) {
        _client.writeWordToPLC(STATE_ERROR, STATE_AREA, STATE_ADDR);
        return;
      }
      // Print Disc Status
      int state = (disc.isBrokenDisc() ? STATE_NOK : STATE_OK);
      _client.writeWordToPLC(state, STATE_AREA, STATE_ADDR);
      
      // Print Errors
      printErrorSegments(disc.getFrontSegments(), F_ERROR_DISP_ADDR);
      printErrorSegments(disc.getBackSegments(),  B_ERROR_DISP_ADDR);
      // Print all data
      printIndexes(disc);
      printMidPositions(disc);
      printOriginalSaliences(disc.getFrontSegments(), P_FSAL_ADDR);
      printOriginalSaliences(disc.getBackSegments(),  P_BSAL_ADDR);
      printOriginalWorkload(disc.getFrontSegments(),  P_FWORK_ADDR);
      printOriginalWorkload(disc.getBackSegments(),   P_BWORK_ADDR);
      
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not check if there's available data.", e);
      throw e;
    }
  }
  
  /**
   * 
   * @param seg_info
   * @param address
   * @throws Exception
   */
  private void printErrorSegments(List<Segment> seg_info, int address) throws Exception {
    int errors = 0;
    for (Segment s : seg_info) {
      if (s.isBroken()) {
        if (++errors > MAX_ERRORS_DISP) {
          _client.writeWordToPLC(STATE_MORE_ERR, STATE_AREA, STATE_ADDR);
          Log.info("NBConsoleRenderer", "Too many errors to display.");
          return;
        }

        // If the work to be displayed is 0.00 (0.001), displays 0.01 instead
        double fixed_work = Math.max(s.getFixedWorkload(), Math.pow(10.0, -DISP_DEC_CASES));

        byte[] data = {
          // index
          Utils.decToHexBytes(s.getIndex() + 1, 2), 
          Utils.decToHexBytes(s.getIndex() + 1, 3),
          // middle position
          Utils.decToHexBytes(s.midPosition(), 2),
          Utils.decToHexBytes(s.midPosition(), 3),
          // original salience
          Utils.decToHexBytes(Utils.doubleToDInt(s.getOriginalSalience(), DISP_DEC_CASES), 2),
          Utils.decToHexBytes(Utils.doubleToDInt(s.getOriginalSalience(), DISP_DEC_CASES), 3),
          // original work
          Utils.decToHexBytes(Utils.doubleToDInt(s.getOriginalWorkload(), DISP_DEC_CASES), 2),
          Utils.decToHexBytes(Utils.doubleToDInt(s.getOriginalWorkload(), DISP_DEC_CASES), 3),
          // correction
          Utils.decToHexBytes(Utils.doubleToDInt(s.correction(), DISP_DEC_CASES), 2),
          Utils.decToHexBytes(Utils.doubleToDInt(s.correction(), DISP_DEC_CASES), 3),
          // fixed workload
          Utils.decToHexBytes(Utils.doubleToDInt(fixed_work, DISP_DEC_CASES), 2),
          Utils.decToHexBytes(Utils.doubleToDInt(fixed_work, DISP_DEC_CASES), 3)
        };
        
        _client.writeAreaToPLC(data, ERROR_DISP_AREA, address);
        address += ERROR_DISP_STEP;
      }
    }
  }
  
  /**
   * 
   * @param disc
   * @throws Exception
   */
  private void printIndexes(SegmentedDisc disc) throws Exception {
    byte[] buffer = new byte[disc.size() * BYTES_PER_WORD];
    for (int i = 0; i < buffer.length; i+= BYTES_PER_WORD) {
      int index = disc.getFrontSegments().get(i / BYTES_PER_WORD).getIndex() + 1;
      buffer[i]     = Utils.decToHexBytes(index, 2);
      buffer[i + 1] = Utils.decToHexBytes(index, 3);
    }
    _client.writeAreaToPLC(buffer, PRINT_AREA, P_INDEX_ADDR);
  }
  
  /**
   * 
   * @param disc
   * @throws Exception
   */
  private void printMidPositions(SegmentedDisc disc) throws Exception {
    byte[] buffer = new byte[disc.size() * BYTES_PER_WORD];
    for (int i = 0; i < buffer.length; i+= BYTES_PER_WORD) {
      int pos = disc.getFrontSegments().get(i / BYTES_PER_WORD).midPosition();
      buffer[i]     = Utils.decToHexBytes(pos, 2);
      buffer[i + 1] = Utils.decToHexBytes(pos, 3);
    }
    _client.writeAreaToPLC(buffer, PRINT_AREA, P_MIDPOS_ADDR);
  }
  
  /**
   * 
   * @param segments
   * @param address
   * @throws Exception
   */
  private void printOriginalSaliences(List<Segment> segments, int address) throws Exception {
    byte[] buffer = new byte[segments.size() * BYTES_PER_WORD];
    for (int i = 0; i < buffer.length; i+= BYTES_PER_WORD) {
      int sal = Utils.doubleToDInt(segments.get(i / BYTES_PER_WORD).getOriginalSalience(), DISP_DEC_CASES);
      buffer[i]     = Utils.decToHexBytes(sal, 2);
      buffer[i + 1] = Utils.decToHexBytes(sal, 3);
    }
    _client.writeAreaToPLC(buffer, PRINT_AREA, address);
  }
  
  /**
   * 
   * @param segments
   * @param address
   * @throws Exception
   */
  private void printOriginalWorkload(List<Segment> segments, int address) throws Exception {
    byte[] buffer = new byte[segments.size() * BYTES_PER_WORD];
    for (int i = 0; i < buffer.length; i+= BYTES_PER_WORD) {
      int work = Utils.doubleToDInt(segments.get(i / BYTES_PER_WORD).getOriginalWorkload(), DISP_DEC_CASES);
      buffer[i]     = Utils.decToHexBytes(work, 2);
      buffer[i + 1] = Utils.decToHexBytes(work, 3);
    }
    _client.writeAreaToPLC(buffer, PRINT_AREA, address);
  }
  
  /**
   * 
   */
  @Override
  public void notifyLoading() throws Exception {
    try {
      _client.writeWordToPLC(STATE_LDING, STATE_AREA, STATE_ADDR);
    } catch (Exception e) {
      Log.error("NetworkDataReceiver", "Could not notity loading.", e);
      throw e;
    }
  }
  
  
  @Override
  public void notifyWorkComplete() throws Exception {
    // EMPTY
  }
  
  // ===== CONSTANTS
  private static final int    PLC_PORT = Configs.getInstance().getInt("PLC_PORT");
  private static final String PLC_IP   = Configs.getInstance().getProperty("PLC_IP");
  
  private static final int    BYTES_PER_WORD = Configs.getInstance().getInt("BYTES_PER_WORD");
  
  // State
  private static final String STATE_AREA     = Configs.getInstance().getProperty("STATE_AREA");
  private static final int    STATE_ADDR     = Configs.getInstance().getInt("STATE_ADDR");
  private static final int    STATE_OK       = Configs.getInstance().getInt("STATE_OK");
  private static final int    STATE_NOK      = Configs.getInstance().getInt("STATE_NOK");
  private static final int    STATE_LDING    = Configs.getInstance().getInt("STATE_LDING");
  private static final int    STATE_ERROR    = Configs.getInstance().getInt("STATE_ERROR");
  private static final int    STATE_MORE_ERR = Configs.getInstance().getInt("STATE_MORE_ERR");
  
  // Print Info
  protected static final int DISP_DEC_CASES   = Configs.getInstance().getInt("DISP_DEC_CASES");
  
  protected static final String PRINT_AREA    = Configs.getInstance().getProperty("PRINT_AREA");
  protected static final int    P_INDEX_ADDR  = Configs.getInstance().getInt("P_INDEX_ADDR");
  protected static final int    P_MIDPOS_ADDR = Configs.getInstance().getInt("P_MIDPOS_ADDR");
  protected static final int    P_FSAL_ADDR   = Configs.getInstance().getInt("P_FSAL_ADDR");
  protected static final int    P_BSAL_ADDR   = Configs.getInstance().getInt("P_BSAL_ADDR");
  protected static final int    P_FWORK_ADDR  = Configs.getInstance().getInt("P_FWORK_ADDR");
  protected static final int    P_BWORK_ADDR  = Configs.getInstance().getInt("P_BWORK_ADDR");
  
  // Errors
  protected static final String ERROR_DISP_AREA   = Configs.getInstance().getProperty("ERROR_DISP_AREA");
  protected static final int    F_ERROR_DISP_ADDR = Configs.getInstance().getInt("F_ERROR_DISP_ADDR");
  protected static final int    B_ERROR_DISP_ADDR = Configs.getInstance().getInt("B_ERROR_DISP_ADDR");
  protected static final int    MAX_ERRORS_DISP   = Configs.getInstance().getInt("MAX_ERRORS_DISP");
  protected static final int    ERROR_DISP_STEP   = Configs.getInstance().getInt("ERROR_DISP_STEP");

  @Override
  public void disconnect() {
   try {
     _client.disconnect();
    } catch (Exception e) {
      Log.warn("NBConsoleRenderer", "Exception thrown while disconnecting.", e);
    }
  }
  
}
