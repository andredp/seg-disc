package com.dsp.renderers;

import com.dsp.analyzer.SegmentedDisc;

public interface DiscRenderer {
  
  public abstract void render(SegmentedDisc disc) throws Exception;
  
}
