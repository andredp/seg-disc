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
    
    Communicator receiver = new NetworkCommunicator();
    
    while (true) {
      try {
        if (!receiver.hasData()) return; // it is blocked here on the Network communicator
        receiver.notifyLoading();
        
        double workTolerance = receiver.getWorkTolerance();
        DiscRawData data     = receiver.receive();
        SegmentedDisc disc   = new SegmentedDisc(data, workTolerance);
        
        Log.info("\n" + disc);
        receiver.printResult(disc);
        receiver.workDone();
        
      } catch (Exception e) {
        e.printStackTrace();
        return;
      }

    }
    
  }

}
