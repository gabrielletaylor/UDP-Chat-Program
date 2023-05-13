// import necessary libraries 
import java.net.*;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.util.*;

public class UDPClient {
  private DatagramSocket socket; // Datagram socket to send/receive packets
  private InetAddress serverAddress; // IP address of server we are communicating with 
  private int serverPort; // port # of server we're communicating with 
  private ArrayList<String> peerInfo;

  // takes IP address and port # of server
  public UDPClient(String serverHost, int serverPort) throws SocketException, UnknownHostException {
    this.socket = new DatagramSocket(); // create new datagram socket 
    this.serverAddress = InetAddress.getByName(serverHost); // convert server hostname to IP address
    this.serverPort = serverPort; // set server port #
  }

  // method to send message to server
  public void send(String message) throws IOException {
    byte[] buffer = message.getBytes(); // convert message to byte array

    // sends message to each peer in peerInfo list
    for (String peer : peerInfo) {
      String[] p = peer.split(" ");
      
      serverAddress = InetAddress.getByName(p[0]); // update serverAddress to peer's IP address
      serverPort = Integer.parseInt(p[1]); //update serverPort to peer's port #
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
      socket.send(packet);// send packet through socket
    }
  }

  // method sends message to server tracker
  public void sendToTracker(String message, InetAddress address, int port) throws IOException {
    byte[] buffer = message.getBytes(); // convert message to byte array
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
    socket.send(packet); // send packet through socket
  }

  // method to receive message from server
  public void receive() throws IOException {
    byte[] buffer = new byte[1024]; // new buffer to store incoming data 
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    socket.receive(packet); // after arrival of packet, store in datagram packet object
    String message = new String(packet.getData());

    if (message.startsWith("peer info")) {
      getPeerInfo(message); // update peerInfo list if message contains peer info 
    }
    else {
      System.out.println(message); // else, print message to console
    }
  }

  // method to extract peer info from message 
  public void getPeerInfo(String message) throws IOException{
    peerInfo = new ArrayList<>();
    String[] peers = message.split("\n");
    for(int i = 1; i < peers.length - 1; i++) {
      peerInfo.add(peers[i]); // add each peer to peerInfo list
    }
  }

  // method to run chat session
  public void runChat() throws IOException {
    System.out.println("Welcome to the Chat Room!"); //client prompts a welcome message 
    System.out.print("Please enter your username: "); // asks enter username using the keyboard

    // read user input for the username
    Scanner scanner = new Scanner(System.in);
    String username = scanner.nextLine();
    sendToTracker(username + " has joined the chat.", InetAddress.getByName("localhost"), 5000);

    System.out.println("Enter messages (type '.' to exit)"); //to exit the chat, user enters "."

    // create new thread to receive messages from server
    new Thread(() -> {
      try {
        while (true) {
          receive();
        }
      } catch (IOException e) {
        
      }
    }).start();

    while (true) {
      String message = scanner.nextLine();

      if (message.equals(".")) {
        sendToTracker(username + " has left the chat.", InetAddress.getByName("localhost"), 5000); // "." indicates that the user has left the chat
        break;
      }

      new Thread(() -> {
        try {
          send(getTimestamp() + " " + username + ": " + message + "");
        } catch (Exception e) {

        }
      }).start();
    }

    scanner.close();
  }

  public String getTimestamp() {
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss"); // display time stamp of message
    return format.format(calendar.getTime());
  }

  // method to close socket
  public void close() {
    socket.close();
  }

  // main method to call functions
  public static void main(String[] args) {
    try {
        UDPClient client = new UDPClient("localhost", 5000);

        client.runChat();

        // close the client socket
        client.close();
        System.out.println("You have left the chat."); // message to display user has left the chat
        System.exit(0);
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
}