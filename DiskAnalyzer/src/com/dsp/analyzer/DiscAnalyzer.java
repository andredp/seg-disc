package com.dsp.analyzer;

import com.dsp.analyzer.config.Configurations;
import com.dsp.communicators.Communicator;
import com.dsp.communicators.NetworkCommunicator;
import com.esotericsoftware.minlog.Log;

public class DiscAnalyzer {

  public static void main(String[] args) throws Exception {
    Communicator plc = null;
    try {
      // Initialization
      Configurations.getInstance().load("config.dat");
      Log.DEBUG(); // debug level
      
      plc = new NetworkCommunicator();
    
      while (true) {
        Log.info("Waiting for available data.");
        if (!plc.hasData()) return; // it is blocked here on the Network communicator
        Log.info("Fetching data.");
        plc.notifyLoading();
        
        double workTolerance = plc.getWorkTolerance();
        DiscRawData data     = plc.receive();
        SegmentedDisc disc   = new SegmentedDisc(data, workTolerance);
        
        Log.info("\n" + disc);
        plc.printResult(disc);
        plc.workDone();
        Log.info("Work done.");
      }
      
    } catch (Exception e) {
      e.printStackTrace();
      return;
    } finally {
      plc.disconnect();
    }
  }
  
}
