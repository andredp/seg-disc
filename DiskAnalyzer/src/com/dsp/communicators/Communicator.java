package com.dsp.communicators;

import com.dsp.analyzer.DiscRawData;
import com.dsp.analyzer.SegmentedDisc;

/**
 * @author andredp
 *
 */
public interface Communicator {
  
  DiscRawData receive() throws Exception;
  
  double getWorkTolerance() throws Exception;
  
  boolean hasData() throws Exception;

  void notifyLoading() throws Exception;

  void workDone() throws Exception;

  void printResult(SegmentedDisc disc) throws Exception;
  
}
