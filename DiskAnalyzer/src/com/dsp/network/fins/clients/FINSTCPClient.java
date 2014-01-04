package com.dsp.network.fins.clients;

import java.io.IOException;

import com.dsp.network.clients.Client;
import com.dsp.network.clients.DebugClient;
import com.dsp.network.clients.TCPClient;
import com.dsp.network.fins.frames.FINSCommandFrame;
import com.dsp.network.fins.frames.FINSCommandResponseFrame;
import com.dsp.network.fins.frames.FINSHeaderFrame;
import com.dsp.network.fins.frames.FINSTCPConnectionResponseFrame;
import com.dsp.network.fins.frames.FINSTCPConnectionFrame;
import com.dsp.network.fins.frames.FINSTCPHeaderFrame;
import com.dsp.network.fins.frames.FINSTCPHeaderResponseFrame;
import com.esotericsoftware.minlog.Log;

public class FINSTCPClient extends FINSClient {

  private Client _client;
  private String    _host;
  private int       _port;
  private boolean   _initialized = false;
  
  // requests
  private FINSTCPHeaderFrame _tcpHeader  = new FINSTCPHeaderFrame();
  private FINSHeaderFrame    _finsHeader = new FINSHeaderFrame();
  private FINSCommandFrame   _command    = new FINSCommandFrame();
  
  // responses
  private FINSTCPHeaderResponseFrame _tcpHeaderResp  = new FINSTCPHeaderResponseFrame();
  private FINSHeaderFrame            _finsHeaderResp = new FINSHeaderFrame();
  private FINSCommandResponseFrame   _commandResp    = new FINSCommandResponseFrame();
  
  public FINSTCPClient(String host, int port) throws Exception {
    _host = host;
    _port = port;
    //_client = new TCPClient(_host, _port);
    _client = new DebugClient();
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
    
    FINSTCPConnectionFrame connectionFrame = new FINSTCPConnectionFrame();
    connectionFrame.setClientNode((byte) 0x00); // automatic node assignment
    _client.send(connectionFrame.getRawFrame());
    
    FINSTCPConnectionResponseFrame connectionResponse = new FINSTCPConnectionResponseFrame();
    _client.receive(connectionResponse.getRawFrame());
    if (connectionResponse.hasError()) { // error check
      Log.error("FINSTCPClient", "FINS/TCP error: " + connectionResponse.getErrorMessage());
      throw new Exception("Could not establish connection.");
    }
    _finsHeader.setServerNode(connectionResponse.getServerNode());
    _finsHeader.setClientNode(connectionResponse.getClientNode());
    
    Log.info("FINSTCPClient", "Connection Successfull!");
    Log.info("FINSTCPClient", "Client Node: " + (int) _finsHeader.getClientNode());
    Log.info("FINSTCPClient", "Server Node: " + (int) _finsHeader.getServerNode());
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
    int length = FINSTCPHeaderFrame.frameLength() + FINSHeaderFrame.frameLength() 
               + FINSCommandFrame.frameLength() + (data != null ? data.length : 0);
    _tcpHeader.setLength(length);
    _command.prepareFrame(type, area, address, words);
    
    // SENDING COMMAND
    _client.send(_tcpHeader.getRawFrame());         // Send TCP Header
    _client.send(_finsHeader.getRawFrame());        // Send FINS Header
    _client.send(_command.getRawFrame());           // Send Command Frame
    if (data != null) _client.send(data);           // Send Data (if there is any)
    
    // RECEIVING COMMAND
    _client.receive(_tcpHeaderResp.getRawFrame());  // Receive TCP Header
    if (_tcpHeaderResp.hasError()) {
      Log.error("TCPFinsClient", "FINS/TCP Send Frame error! <code: " + _tcpHeaderResp.getErrorCode());
      throw new Exception("Couldn't read data.");
    }
    _client.receive(_finsHeaderResp.getRawFrame()); // Receive FINS Header
    _client.receive(_commandResp.getRawFrame());    // Receive Command Response
    _commandResp.prepareDataBuffer(_tcpHeaderResp.getDataLength());
    _client.receive(_commandResp.getDataBuffer());  // Receive Data (if it needs)
    
    return _commandResp;
  }

}
