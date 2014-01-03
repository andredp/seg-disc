package com.dsp.receivers;

import com.dsp.analyzer.DiscRawData;

/**
 * @author andredp
 *
 */
public interface Receiver {
  
  boolean     hasData()            throws Exception;
  void        waitForData()        throws Exception;
  double      getWorkTolerance()   throws Exception;
  DiscRawData receive()            throws Exception;
  void        signalWorkComplete() throws Exception;
  void        disconnect();
  
}
