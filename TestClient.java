// import necessary libraries 
import java.net.*;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.util.*;

public class TestClient implements Runnable {
    private DatagramSocket socket; // Datagram socket to send/receive packets
    private InetAddress serverAddress; // IP address of server we are communicating with 
    private int serverPort; // port # of server we're communicating with 
    private ArrayList<String> peerInfo;
    private String username;

    // takes IP address and port # of sserver
    public TestClient(String username, String serverHost, int serverPort) throws SocketException, UnknownHostException {
        this.username = username;
        this.socket = new DatagramSocket(); // create new datagram socket 
        this.serverAddress = InetAddress.getByName(serverHost); // convert server hostname to IP address
        this.serverPort = serverPort; // set server port #
    }

    // method to send message to server
    public void send(String message) throws IOException {
        byte[] buffer = message.getBytes(); // convert message to byte array

        for (String peer : peerInfo) {
        String[] p = peer.split(" ");
        
        serverAddress = InetAddress.getByName(p[0]);
        serverPort = Integer.parseInt(p[1]);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
        socket.send(packet);
        }
    }

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
            getPeerInfo(message);
        }
    }

    public void getPeerInfo(String message) throws IOException{
        peerInfo = new ArrayList<>();
        String[] peers = message.split("\n");
        for(int i = 1; i < peers.length - 1; i++) {
            peerInfo.add(peers[i]);
        }
    }

    public String getTimestamp() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(calendar.getTime());
    }

    // method to close socket
    public void close() {
        socket.close();
    }

    @Override
    public void run() {
        try {
            sendToTracker(username + " has joined the chat.", InetAddress.getByName("localhost"), 5000);
            Thread.sleep(3000);

            Thread thread1 = new Thread() {
                public void run() {
                    try {
                        while (true) {
                            receive();
                        }
                    } catch (IOException e) {}
                }
            };
            thread1.start();

            for (int i = 0; i < 3; i++) {
                send(getTimestamp() + " " + username + ": test message");
                Thread.sleep(3000);
            }
            sendToTracker(username + " has left the chat.", InetAddress.getByName("localhost"), 5000);
        } catch (Exception e) {}
    }

    // main method to call functions
    public static void main(String[] args) throws InterruptedException, IOException {
        for (int i = 0; i < 50; i++) {
            TestClient test = new TestClient("peer" + i, "localhost", 5000);
            Thread thread = new Thread(test);
            thread.start();
        }
    }
}
