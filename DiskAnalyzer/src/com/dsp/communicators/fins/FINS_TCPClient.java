package com.dsp.communicators.fins;

import com.dsp.clients.TCPClient;
import com.esotericsoftware.minlog.Log;

public class FINS_TCPClient extends FINSClient {

  private TCPClient _client       = null;
  private byte[] _fins_header     = null;
  private byte[] _read_tcp_header = null; 
  
  public FINS_TCPClient(String host, int port) throws Exception {
    _client = new TCPClient(host, port);
    _read_tcp_header = FINSFrames.createTCPSendFrame(FINSFrames.SEND_FRAME_LENGTH);
  }
  
  @Override
  public void connect() throws Exception {
    
    _client.send(FINSFrames.createTCPConnectFrame(0));
    byte[] tcp_frame = new byte[24];
    _client.receive(tcp_frame);
    
    if (FINSFrames.tcpHeaderErrorCode(tcp_frame) != 0) {
      Log.error("TCPFinsClient", "FINS/TCP illegal command error");
      throw new Exception("Could not establish connection.");
    }
    
    byte clientNode = tcp_frame[FINSFrames.FTF_CLIN + 3];
    byte serverNode = tcp_frame[FINSFrames.FTF_SRVN + 3];
    _fins_header = FINSFrames.createFinsHeaderFrame(clientNode, serverNode, 0);
    
    Log.info("TCPFinsClient", "Handshake successfull");
    Log.info("TCPFinsClient", "Client Node: " + clientNode);
    Log.info("TCPFinsClient", "Server Node: " + serverNode); // ola
  }

  
  @Override
  public void disconnect() throws Exception {
    _client.close();
  }

  /**
   * 
   */
  @Override
  protected byte[] getReadResponseFrame(String area, int offset, int words) throws Exception {
    // requesting positions
    _client.send(_read_tcp_header);
    _client.send(_fins_header);
    _client.send(FINSFrames.createCommandFrame("area_read", area, offset, words));
    
    // getting the TCP header
    byte[] header_resp = new byte[FINSFrames.FINS_TCP_SEND_FRAME.length];
    _client.receive(header_resp);
    if (FINSFrames.tcpHeaderErrorCode(header_resp) != 0) {
      Log.error("TCPFinsClient", "FINS/TCP Send Frame error. CODE: " 
          + FINSFrames.tcpHeaderErrorCode(header_resp));
      throw new Exception("Couldn't read data.");
    }
    
    // getting the actual response frame
    int length  = ((int) header_resp[FINSFrames.FTF_LENG + 2] & 0xff) << 8;
        length |= ((int) header_resp[FINSFrames.FTF_LENG + 3] & 0xff);
        length -= FINSFrames.FTF_DATALENGTH;
    byte[] response = new byte[length];
    _client.receive(response);
    
    byte[] response_frame = new byte[length - FINSFrames.FINS_HEADER.length];
    for (int i = 0; i < response_frame.length; i++) {
      response_frame[i] = response[i + FINSFrames.FINS_HEADER.length];
    }
    
    return response_frame;
  }
  
  
  /**
   * 
   */
 @Override
  protected byte[] getWriteResponseFrame(byte[] data, String area, int address) throws Exception {
    int words = data.length / 2;
    // sending the write request
    _client.send(FINSFrames.createTCPSendFrame(FINSFrames.SEND_FRAME_LENGTH + words));
    _client.send(_fins_header);
    _client.send(FINSFrames.createCommandFrame("area_write", area, address, words));
    _client.send(data);
    
    // getting the TCP header
    byte[] header_resp = new byte[FINSFrames.FINS_TCP_SEND_FRAME.length];
    _client.receive(header_resp);
    if (FINSFrames.tcpHeaderErrorCode(header_resp) != 0) {
      Log.error("TCPFinsClient", "FINS/TCP Send Frame error. CODE: " 
          + FINSFrames.tcpHeaderErrorCode(header_resp));
      throw new Exception("Couldn't write the data.");
    }
    
    // getting the actual response frame
    int length  = ((int) header_resp[FINSFrames.FTF_LENG + 2] & 0xff) << 8;
        length |= ((int) header_resp[FINSFrames.FTF_LENG + 3] & 0xff);
        length -= FINSFrames.FTF_DATALENGTH;
    byte[] response = new byte[length];
    _client.receive(response);
    
    return response;
  }

}
