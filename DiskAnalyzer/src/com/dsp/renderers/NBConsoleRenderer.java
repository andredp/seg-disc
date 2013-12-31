package com.dsp.renderers;

import java.util.List;

import com.dsp.analyzer.Segment;
import com.dsp.analyzer.SegmentedDisc;
import com.dsp.analyzer.config.Configurations;
import com.dsp.communicators.fins.FINSClient;
import com.dsp.communicators.fins.FINSFrames;
import com.dsp.libs.Utils;
import com.esotericsoftware.minlog.Log;

public class NBConsoleRenderer implements DiscRenderer {
  
  private FINSClient _client; 

  public NBConsoleRenderer(FINSClient client) {
    _client = client;
  }

  @Override
  public void render(SegmentedDisc disc) throws Exception {
    try {
      _client.testOrConnect();
      
      // Print Errors
      printErrorSegments(disc.getFrontSegments(), F_ERROR_DISP_ADDR);
      printErrorSegments(disc.getBackSegments(),  B_ERROR_DISP_ADDR);
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
          Log.warn("Too many errors!");
          return;
        }

        byte[] index    = FINSFrames.decToHexBytes(s.getIndex() + 1);
        byte[] pos      = FINSFrames.decToHexBytes(s.midPosition());
        byte[] orig_sal = FINSFrames.decToHexBytes(Utils.doubleToDInt(s.getOriginalSalience(), DISP_DEC_CASES));
        byte[] orig_wrk = FINSFrames.decToHexBytes(Utils.doubleToDInt(s.getOriginalWorkload(), DISP_DEC_CASES));
        byte[] correct  = FINSFrames.decToHexBytes(Utils.doubleToDInt(s.correction(),          DISP_DEC_CASES));
        byte[] fix_wrk  = FINSFrames.decToHexBytes(Utils.doubleToDInt(s.getFixedWorkload(),    DISP_DEC_CASES));
        
        byte[] data = {
          index[2],    index[3],
          pos[2],      pos[3],
          orig_sal[2], orig_sal[3],
          orig_wrk[2], orig_wrk[3],
          correct[2],  correct[3],
          fix_wrk[2],  fix_wrk[3],
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
    byte[] buffer = new byte[disc.size() * FINSFrames.BYTES_PER_WORD];
    for (int i = 0; i < buffer.length; i+= FINSFrames.BYTES_PER_WORD) {
      byte[] word = FINSFrames.decToHexBytes((i / FINSFrames.BYTES_PER_WORD) + 1);
      buffer[i]     = word[2];
      buffer[i + 1] = word[3];
    }
    _client.writeAreaToPLC(buffer, PRINT_AREA, P_INDEX_ADDR);
  }
  
  /**
   * 
   * @param disc
   * @throws Exception
   */
  private void printMidPositions(SegmentedDisc disc) throws Exception {
    byte[] buffer = new byte[disc.size() * FINSFrames.BYTES_PER_WORD];
    for (int i = 0; i < buffer.length; i+= FINSFrames.BYTES_PER_WORD) {
      int pos = disc.getFrontSegments().get(i / FINSFrames.BYTES_PER_WORD).midPosition();
      byte[] word = FINSFrames.decToHexBytes(pos);
      buffer[i]     = word[2];
      buffer[i + 1] = word[3];
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
    byte[] buffer = new byte[segments.size() * FINSFrames.BYTES_PER_WORD];
    for (int i = 0; i < buffer.length; i+= FINSFrames.BYTES_PER_WORD) {
      int sal = Utils.doubleToDInt(segments.get(i / FINSFrames.BYTES_PER_WORD).getOriginalSalience(), DISP_DEC_CASES);
      byte[] word = FINSFrames.decToHexBytes(sal);
      buffer[i]     = word[2];
      buffer[i + 1] = word[3];
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
    byte[] buffer = new byte[segments.size() * FINSFrames.BYTES_PER_WORD];
    for (int i = 0; i < buffer.length; i+= FINSFrames.BYTES_PER_WORD) {
      int work = Utils.doubleToDInt(segments.get(i / FINSFrames.BYTES_PER_WORD).getOriginalWorkload(), DISP_DEC_CASES);
      byte[] word = FINSFrames.decToHexBytes(work);
      buffer[i]     = word[2];
      buffer[i + 1] = word[3];
    }
    _client.writeAreaToPLC(buffer, PRINT_AREA, address);
  }
  
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
  protected static final String ERROR_DISP_AREA   = Configurations.getInstance().getProperty("ERROR_DISP_AREA");
  protected static final int    F_ERROR_DISP_ADDR = Configurations.getInstance().getInt("F_ERROR_DISP_ADDR");
  protected static final int    B_ERROR_DISP_ADDR = Configurations.getInstance().getInt("B_ERROR_DISP_ADDR");
  protected static final int    MAX_ERRORS_DISP   = Configurations.getInstance().getInt("MAX_ERRORS_DISP");
  protected static final int    ERROR_DISP_STEP   = Configurations.getInstance().getInt("ERROR_DISP_STEP");
  
}
