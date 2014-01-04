package com.dsp.network.clients;

import com.dsp.libs.Utils;

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
    System.out.print("SEND: ");
    System.out.println(Utils.arrayToHexString(data));
  }

  @Override
  public void receive(byte[] response) throws Exception {
    System.out.print("RCVD: ");
    System.out.println(Utils.arrayToHexString(response));
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub
    
  }

}
