package com.dsp.network.fins.clients;

import com.dsp.network.fins.frames.FINSCommandResponseFrame;


public class FINS_UDPClient extends FINSClient {
  
  @Override
  public void connect() throws Exception {
 // TODO Auto-generated method stub
  }


  @Override
  public void disconnect() throws Exception {
 // TODO Auto-generated method stub
  }


  @Override
  protected FINSCommandResponseFrame sendCommand(String type, String area,
      int address, int words) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  protected FINSCommandResponseFrame sendCommand(String type, String area,
      int address, int words, byte[] data) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }


}
