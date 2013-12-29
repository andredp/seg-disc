package com.dsp.communicators;

import com.dsp.analyzer.DiscRawData;
import com.dsp.analyzer.SegmentedDisc;

public class DummyCommunicator implements Communicator {

  private static final double[] VALUE_RANGE = { 0.0, 2.0 };

  private static final double[] ERROR_RANGE = { -5.0, 0.0 };

  private boolean _hasData = true;
  
  /**
   * 
   * @param min
   * @param max
   * @return
   */
  private double randomBetween(double min, double max) {
    return min + (max - min) * Math.random();
  }

  /**
   * 
   */
  @Override
  public DiscRawData receive() throws Exception {
    return new DiscRawData(createVector(), createVector());
  }

  private double[] createVector() {
    double[] vector = new double[5000];
    
    for (int ix = 0; ix < vector.length; /* ix++ */) {
      int error_spaces   = (int)randomBetween(34.0, 38.0);
      int correct_spaces = (int)randomBetween(34.0, 36.0);
      
      for (int jx = 0; jx <= error_spaces && ix < vector.length; jx++, ix++) {
        vector[ix] = randomBetween(ERROR_RANGE[0], ERROR_RANGE[1]);
      }
      
      for (int jx = 0; jx <= correct_spaces && ix < vector.length; jx++, ix++) {
        if (Math.random() < 0.05) {
          vector[ix] = randomBetween(VALUE_RANGE[1], VALUE_RANGE[1] * 2.0);
        } else {
          vector[ix] = randomBetween(VALUE_RANGE[0], VALUE_RANGE[1]);
        }
      }

    }

    return vector;
  }

  @Override
  public boolean hasData() throws Exception {
    return _hasData;
  }

  @Override
  public double getWorkTolerance() throws Exception {
    return 0.05;
  }

  @Override
  public void notifyLoading() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void workDone() throws Exception {
    _hasData = false;
  }

  @Override
  public void printResult(SegmentedDisc disc) throws Exception {
    // TODO Auto-generated method stub
    
  }
  
}
