package com.dsp.network.clients;

public class DebugClient implements Client {

  @Override
  public String getHost() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getPort() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void send(byte[] data) throws Exception {
    System.out.println(data);
  }

  @Override
  public void receive(byte[] response) throws Exception {
    // TODO Auto-generated method stub
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub
    
  }

}
