package com.dsp.network.fins.clients;

import java.io.IOException;

import com.dsp.network.clients.Client;
import com.dsp.network.clients.TCPClient;
import com.dsp.network.fins.frames.FINSCommandFrame;
import com.dsp.network.fins.frames.FINSCommandFrame.CommandType;
import com.dsp.network.fins.frames.FINSCommandResponseFrame;
import com.dsp.network.fins.frames.FINSHeaderFrame;
import com.dsp.network.fins.frames.FINSTCPConnectionFrame;
import com.dsp.network.fins.frames.FINSTCPConnectionResponseFrame;
import com.dsp.network.fins.frames.FINSTCPHeaderFrame;
import com.dsp.network.fins.frames.FINSTCPHeaderResponseFrame;
import com.esotericsoftware.minlog.Log;

public class FINSTCPClient extends FINSClient {

  private Client  _client;
  private String  _host;
  private int     _port;
  
  // requests
  private FINSTCPHeaderFrame _tcpHeader  = new FINSTCPHeaderFrame();
  private FINSHeaderFrame    _finsHeader = new FINSHeaderFrame();
  private FINSCommandFrame   _command    = new FINSCommandFrame();
  
  // responses
  private FINSTCPHeaderResponseFrame _tcpHeaderResp  = new FINSTCPHeaderResponseFrame();
  private FINSHeaderFrame            _finsHeaderResp = new FINSHeaderFrame();
  private FINSCommandResponseFrame   _commandResp    = new FINSCommandResponseFrame();
  
  /**
   * This class is a client to the Omron's PLC (pc1l-e) that allows arrays, words or bits to be 
   * written or read from it.
   * 
   * @param host  the PLC's IP address
   * @param port  the PLC's port
   * @throws Exception
   */
  public FINSTCPClient(String host, int port) throws Exception {
    _host = host;
    _port = port;
    connect();
  }
  
  /**
   * Establishes a connection to the PLC (handshake) with the ability to force it
   * 
   * @param forceReconnection  when true, forces a full reconnection to the host
   * @throws Exception
   */
  private void connect() throws Exception {
    if (_client != null) {
      _client.close();
    }
    _client = new TCPClient(_host, _port);
    
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
   * Disconnects from the host
   */
  @Override
  public void disconnect() throws Exception {
    _client.close();
  }
  
  /**
   * Used to send read commands (where there is no data to be written in the PLC)
   * 
   * @return the Command Response Frame containing the actual response and the data read
   */
  @Override
  public FINSCommandResponseFrame sendCommand(CommandType type, String area, int address, int words) throws Exception {
    return sendCommand(type, area, address, words, null);
  }
  
  /**
   * Used to send write commands (wraps the internalSendCommand to check for IOExceptions from the
   * socket, retrying the connection when that happens)
   * 
   * @see internalSendCommand
   * @return the Command Response Frame containing the actual response 
   */
  @Override
  public FINSCommandResponseFrame sendCommand(CommandType type, String area, int address, int words, byte[] data) throws Exception {
    int tries = 2;
    while (tries >= 0) {
      try {
        return internalSendCommand(type, area, address, words, data);
      } catch (IOException e) {
        connect();
        tries--;
      }
    }
    // in case it cannot connect in the number of tries allowed
    throw new Exception("Couldn't connect to the host.");
  }
  
  /**
   * 
   * 
   * @param type
   * @param area
   * @param address
   * @param words
   * @param data
   * @return
   * @throws Exception
   */
  private FINSCommandResponseFrame internalSendCommand(CommandType type, String area, int address, int words, byte data[]) throws Exception {
    int length = FINSTCPHeaderFrame.frameLength() + FINSHeaderFrame.frameLength() 
               + FINSCommandFrame.frameLength() + (data != null ? data.length : 0);
    _tcpHeader.setLength(length);
    _command.prepareFrame(type, area, address, words);
    
    // SENDING COMMAND
    _client.send(_tcpHeader.getRawFrame());         // Send TCP Header
    _client.send(_finsHeader.getRawFrame());        // Send FINS Header
    _client.send(_command.getRawFrame());           // Send Command Frame
    if (data != null) _client.send(data);           // Send Data (if there is any)
    
    // RECEIVING RESPONSE
    _client.receive(_tcpHeaderResp.getRawFrame());  // Receive TCP Header
    if (_tcpHeaderResp.hasError()) {
      Log.error("TCPFinsClient", "FINS/TCP Send Frame error: " + _tcpHeaderResp.getErrorMessage());
      throw new Exception("Couldn't read data.");
    }
    _client.receive(_finsHeaderResp.getRawFrame()); // Receive FINS Header
    _client.receive(_commandResp.getRawFrame());    // Receive Command Response
    _commandResp.prepareDataBuffer(_tcpHeaderResp.getDataLength());
    _client.receive(_commandResp.getDataBuffer());  // Receive Data (if it needs)
    
    return _commandResp;
  }

}
