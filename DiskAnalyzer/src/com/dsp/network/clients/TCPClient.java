package com.dsp.network.clients;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.esotericsoftware.minlog.Log;

public class TCPClient implements Client {

  private Socket _socket = null;
  private DataOutputStream _output = null;
  private DataInputStream  _input  = null;

  public TCPClient(String host, int port) throws Exception {
    try {
      // Creating the socket and sets the options
      _socket = new Socket(host, port);
      //_socket.setKeepAlive(keepAlive);
      // Streams
      _output = new DataOutputStream(_socket.getOutputStream());
      _input  = new DataInputStream(_socket.getInputStream());
      // Log
      Log.info("TCPClient", "Client successfuly created to "
          + _socket.getInetAddress().getHostName() + ":" + _socket.getPort());
    } catch (SocketException e) {
      Log.error("TCPClient", "Could not create a datagram socket", e);
      throw e;
    } catch (UnknownHostException e) {
      Log.error("TCPClient", "Could not resolve the host name: " + host, e);
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
  public void send(byte[] data) throws IOException {
    try {
      _output.write(data);
    } catch (IOException e) {
      Log.warn("TCPClient", "Could not send the packet.", e);
      throw e;
    }
  }

  @Override
  public void receive(byte[] response) throws IOException {
    try {
      _input.readFully(response);
    } catch (IOException e) {
      Log.warn("TCPClient", "Could not receive the packet", e);
      throw e;
    }
  }

  @Override
  public void close() {
    try {
      _socket.close();
    } catch (IOException e) {
      Log.error("TCPClient", "Error closing the socket.", e);
    }
  }

}
