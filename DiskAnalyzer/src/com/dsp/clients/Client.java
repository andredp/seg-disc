package com.dsp.clients;

public interface Client {
  
  String getHost();  
  int    getPort();
    
  void send(byte[] data) throws Exception;  
  void receive(byte[] response) throws Exception;
    
  boolean ping();
  
  void close();
  
}
