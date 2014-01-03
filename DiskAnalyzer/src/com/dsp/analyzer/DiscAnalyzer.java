package com.dsp.analyzer;

import com.dsp.config.Configurations;
import com.dsp.receivers.NetworkReceiver;
import com.dsp.receivers.Receiver;
import com.dsp.renderers.DiscRenderer;
import com.dsp.renderers.NBConsoleRenderer;
import com.esotericsoftware.minlog.Log;

public class DiscAnalyzer {
  
  private Receiver     _plc;
  private DiscRenderer _console;
  
  void startAnalyzing() {
    try {
      // Initialization
      Configurations.getInstance().load("config.dat");
      Log.DEBUG(); // debug level
      
      _plc     = new NetworkReceiver();
      _console = new NBConsoleRenderer();
      
      while (true) {
        Log.info("Waiting for available data.");
        _plc.waitForData();
        Log.info("Preparing to receive the disc data...");
        _console.notifyLoading();
        Log.info("Reading the Work Tolerance value.");
        double workTolerance = _plc.getWorkTolerance();        
        Log.info("Work Tolerance: " + workTolerance * 100.0 + "%");
        Log.info("Receiving data...");
        DiscRawData data   = _plc.receive();
        Log.info("Download complete.");
        SegmentedDisc disc = new SegmentedDisc(data, workTolerance);
        Log.info("Calculations complete.");
        Log.debug("\n" + disc);
        Log.info("Writing results to the output.");
        _console.render(disc);
        Log.info("Signaling the PLC that the work is done so another test can take place.");
        _plc.signalWorkComplete();
        _console.notifyWorkComplete();
        Log.info("Work done.");
      }
      
    } catch (Exception e) {
      e.printStackTrace();
      return;
    } finally {
      _plc.disconnect();
      _console.disconnect();
    }
  }

  /**
   * Starting point
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    new DiscAnalyzer().startAnalyzing();
  }
  
}
