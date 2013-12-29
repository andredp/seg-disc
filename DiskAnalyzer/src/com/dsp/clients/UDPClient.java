package com.dsp.clients;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.dsp.analyzer.config.Configurations;
import com.dsp.communicators.exceptions.UDPRetriesExceededException;
import com.esotericsoftware.minlog.Log;

public class UDPClient implements Client {

  private static final int UDP_RETRIES = Configurations.getInstance().getInt("UDP_RETRIES");
  private static final int UDP_TIMEOUT = Configurations.getInstance().getInt("UDP_TIMEOUT");

  private DatagramSocket _socket = null;

  public UDPClient(String host, int port) throws Exception {
    try {
      // Creating the datagram socket
      _socket = new DatagramSocket(port, InetAddress.getByName(host));
      _socket.setSoTimeout(UDP_TIMEOUT);
      // Log
      Log.info("UDPClient", "Client successfuly created to "
          + _socket.getInetAddress().getHostName() + ":" + _socket.getPort()
          + ". <timeout = " + _socket.getSoTimeout() + ">");
     
    } catch (SocketException e) {
      Log.error("UDPClient", "Could not create a datagram socket", e);
      throw e;
    } catch (UnknownHostException e) {
      Log.error("UDPClient", "Could not resolve the host name: " + host, e);
      throw e;
    }
  }
  
  @Override
  public String getHost() {
    return _socket.getInetAddress().getHostName();
  }

  @Override
  public int getPort() {
    return _socket.getPort();
  }

  @Override
  public void send(byte[] data) throws Exception {
    try {
      DatagramPacket packet = new DatagramPacket(data, data.length);
      _socket.send(packet);
    } catch (Exception e) {
      Log.warn("UDPClient", "Could not send the packet", e);
      throw e;
    }
  }

  @Override
  public void receive(byte[] response) throws IOException {
    try {
      DatagramPacket packet = new DatagramPacket(response, response.length);
      _socket.receive(packet);
    } catch (IOException e) {
      Log.warn("UDPClient", "Could not receive the packet", e);
      throw e;
    }
  }

  public void sendAndReceive(byte[] data, byte[] response) throws UDPRetriesExceededException {
    for (int errors = 0; errors < UDP_RETRIES; errors++) {
      // Sends the data
      try {
        send(data);
        receive(response);
        return;
      } catch (Exception e) {
        continue;
      }
    }
    // Exceeded retries
    throw new UDPRetriesExceededException();
  }

  @Override
  public void close() {
    _socket.close();
  }

}
