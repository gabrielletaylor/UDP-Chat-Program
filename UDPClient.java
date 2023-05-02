// import necessary libraries 
import java.net.*;
import java.io.IOException;

public class UDPClient {
  private DatagramSocket socket; // Datagram socket to send/receive packets
  private InetAddress serverAddress; // IP address of server we are communicating with 
  private int serverPort; // port # of server we're communicating with 

  // takes IP address and port # of sserver
  public UDPClient(String serverHost, int serverPort) throws SocketException, UnknownHostException {
    this.socket = new DatagramSocket(); // create new datagram socket 
    this.serverAddress = InetAddress.getByName(serverHost); // convert server hostname to IP address
    this.serverPort = serverPort; // set server port #
  }

  // method to send message to server
  public void send(String message) throws IOException {
    byte[] buffer = message.getBytes(); // convert message to byte array
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
    socket.send(packet); // send packet through socket
  }

  // method to receive message from server
  public String receive() throws IOException {
    byte[] buffer = new byte[1024]; // new buffer to store incoming data 
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    socket.receive(packet); // after arrival of packet, store in datagram packet object
    return new String(packet.getData(), 0, packet.getLength());
  }

  // method to close socket
  public void close() {
    socket.close();
  }

  // main method to call functions
  public static void main(String[] args) {
    try {
        UDPClient client = new UDPClient("localhost", 5000);

        // send a message to the server
        String message = "Hello, server!"; // example message 
        client.send(message);

        // receive a response from the server
        String response = client.receive();
        System.out.println("Server response: " + response); // display response

        // close the client socket
        client.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
}