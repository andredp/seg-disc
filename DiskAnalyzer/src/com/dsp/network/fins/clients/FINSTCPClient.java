package com.dsp.network.fins.clients;

import java.io.IOException;

import com.dsp.network.clients.TCPClient;
import com.dsp.network.fins.frames.FINSCommandFrame;
import com.dsp.network.fins.frames.FINSCommandResponseFrame;
import com.dsp.network.fins.frames.FINSHeaderFrame;
import com.dsp.network.fins.frames.FINSTCPHeaderConnResponseFrame;
import com.dsp.network.fins.frames.FINSTCPHeaderConnectionFrame;
import com.dsp.network.fins.frames.FINSTCPHeaderSendFrame;
import com.esotericsoftware.minlog.Log;

public class FINSTCPClient extends FINSClient {

  private TCPClient _client;
  private String    _host;
  private int       _port;
  private boolean   _initialized = false;
  
  // requests
  private FINSTCPHeaderSendFrame _tcpHeaderFrame  = new FINSTCPHeaderSendFrame();
  private FINSHeaderFrame        _finsHeaderFrame = new FINSHeaderFrame();
  private FINSCommandFrame       _commandFrame    = new FINSCommandFrame();
  
  // responses
  private FINSTCPHeaderSendFrame   _tcpRespFrame  = new FINSTCPHeaderSendFrame();
  private FINSHeaderFrame          _finsRespFrame = new FINSHeaderFrame();
  private FINSCommandResponseFrame _commRespFrame = new FINSCommandResponseFrame();
  
  public FINSTCPClient(String host, int port) throws Exception {
    _host = host;
    _port = port;
    _client = new TCPClient(_host, _port);
  }
  
  /**
   * 
   */
  @Override
  public void connect() throws Exception {
    connect(false);
  }
  
  /**
   * 
   * @param forceReconnection
   * @throws Exception
   */
  private void connect(boolean forceReconnection) throws Exception {
    if (_initialized && !forceReconnection) return;
    _initialized = true;
    
    if (forceReconnection) {
      _client.close();
      _client = new TCPClient(_host, _port);
    }
    
    FINSTCPHeaderConnectionFrame connFrame = new FINSTCPHeaderConnectionFrame();
    connFrame.setClientNode((byte) 0x00); // automatic node assignment
    _client.send(connFrame.getRawFrame());
    
    FINSTCPHeaderConnResponseFrame connResponse = new FINSTCPHeaderConnResponseFrame();
    _client.receive(connResponse.getRawFrame());
    if (connResponse.hasError()) { // error check
      Log.error("TCPFinsClient", "FINS/TCP error. Could not connect to host.");
      throw new Exception("Could not establish connection.");
    }
    _finsHeaderFrame.setDA1(connResponse.getServerNode());
    _finsHeaderFrame.setSA1(connResponse.getClientNode());
    
    Log.info("TCPFinsClient", "Connection Successfull!");
    Log.info("\tClient Node: " + _finsHeaderFrame.getSA1());
    Log.info("\tServer Node: " + _finsHeaderFrame.getDA1());
  }

  /**
   * 
   */
  @Override
  public void disconnect() throws Exception {
    _client.close();
  }
  
  /**
   * 
   */
  @Override
  public FINSCommandResponseFrame sendCommand(String type, String area, int address, int words) throws Exception {
    return sendCommand(type, area, address, words, null);
  }
  
  /**
   * 
   */
  @Override
  public FINSCommandResponseFrame sendCommand(String type, String area, int address, int words, byte[] data) throws Exception {
    int tries = 2;
    while (tries >= 0) {
      try {
        return internalSendCommand(type, area, address, words, data);
      } catch (IOException e) {
        connect(true);
        tries--;
      }
    }
    // in case it cannot connect in the number of tries allowed
    throw new Exception("Couldn't connect to the host.");
  }
  
  /**
   * 
   * @param type
   * @param area
   * @param address
   * @param words
   * @param data
   * @return
   * @throws Exception
   */
  private FINSCommandResponseFrame internalSendCommand(String type, String area, int address, int words, byte data[]) throws Exception {
    int length = FINSTCPHeaderSendFrame.frameLength() + FINSHeaderFrame.frameLength() 
               + FINSCommandFrame.frameLength() + (data != null ? data.length : 0);
    _tcpHeaderFrame.setLength(length);
    _commandFrame.prepareFrame(type, area, address, words);
    
    // SENDING COMMAND
    _client.send(_tcpHeaderFrame.getRawFrame());      // Send TCP Header
    _client.send(_finsHeaderFrame.getRawFrame());     // Send FINS Header
    _client.send(_commandFrame.getRawFrame());        // Send Command Frame
    if (data != null) _client.send(data);             // Send Data (if there is any)
    
    // RECEIVING COMMAND
    _client.receive(_tcpRespFrame.getRawFrame());     // Receive TCP Header
    if (_tcpRespFrame.hasError()) {
      Log.error("TCPFinsClient", "FINS/TCP Send Frame error! <code: " + _tcpRespFrame.getErrorCode());
      throw new Exception("Couldn't read data.");
    }
    _client.receive(_finsRespFrame.getRawFrame());    // Receive FINS Header
    _commRespFrame.prepareDataBuffer(_tcpRespFrame.getDataLength());
    _client.receive(_commRespFrame.getRawFrame());    // Receive Command Response
    _client.receive(_commRespFrame.getDataBuffer());  // Receive Data (if it needs)
    
    return _commRespFrame;
  }

}
