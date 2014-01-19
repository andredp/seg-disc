package com.dsp.network.fins.clients;

import com.dsp.network.fins.frames.FINSCommandFrame.CommandType;
import com.dsp.network.fins.frames.FINSCommandResponseFrame;


public class FINSUDPClient extends FINSClient {

  @Override
  public void disconnect() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected FINSCommandResponseFrame sendCommand(CommandType type, String area,
      int address, int words) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected FINSCommandResponseFrame sendCommand(CommandType type, String area,
      int address, int words, byte[] data) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
