package com.dsp.analyzer;

import java.io.IOException;

import com.dsp.analyzer.config.Configurations;
import com.dsp.communicators.Communicator;
import com.dsp.communicators.NetworkCommunicator;
import com.esotericsoftware.minlog.Log;

public class DiscAnalyzer {

  public static void main(String[] args) throws IOException {
    // Initialization
    Configurations.getInstance().load("config.dat");
    Log.DEBUG(); // debug level
    
    Communicator plc = null;
    try {
      plc = new NetworkCommunicator();
    } catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    while (true) {
      try {
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
        
      } catch (Exception e) {
        e.printStackTrace();
        return;
      }

    }
    
  }

}
