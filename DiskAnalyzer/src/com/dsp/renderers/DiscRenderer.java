package com.dsp.renderers;

import com.dsp.analyzer.SegmentedDisc;

public interface DiscRenderer {
  
  void notifyLoading()            throws Exception;
  void render(SegmentedDisc disc) throws Exception;
  void notifyWorkComplete()       throws Exception;
  void disconnect();
  
}
