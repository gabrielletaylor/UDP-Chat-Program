import java.io.*;
import java.net.*;
import java.util.*;

public class Tracker implements Runnable {

    private DatagramSocket tracker;
    private DatagramPacket receivePacket, sendPacket;
    private HashMap<String, HashMap<InetAddress, Integer>> peers = new HashMap<>();
    private final int BUFFER_SIZE = 1024;

    // constructor to create datagram socket at a given port
    public Tracker(int port) {
        try {
            this.tracker = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    // method to send a message to all peers
    public synchronized void sendMessage(String message) throws IOException {
        // convert the message to bytes
        byte[] sendData = new byte[BUFFER_SIZE];
        sendData = message.getBytes();

        // loop through the usernames and their respective peer info
        for (HashMap.Entry<String, HashMap<InetAddress, Integer>> entry : peers.entrySet()) {
            // loop through the peer's IP address and their respective port
            for (HashMap.Entry<InetAddress, Integer> e : entry.getValue().entrySet()) {
                InetAddress IPAddress = e.getKey();
                int port = e.getValue();
                // create datagram to send to send to peer
                sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                // write out message datagram to socket
                tracker.send(sendPacket);
            }
        }
    }

    // method to receive a message from a peer
    public void receiveMessage() throws IOException {
        // create space for received messages
        byte[] receiveData = new byte[BUFFER_SIZE];
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        // receive the incoming datagram/message from peer
        tracker.receive(receivePacket);
        // get the IP address and port number of peer sender
        InetAddress IPAddress = receivePacket.getAddress();
        int port = receivePacket.getPort();

        // convert the received datagram from bytes to String
        String message = new String(receivePacket.getData());
        // a new peer has joined the chat
        if (message.contains("has joined the chat")) {
            // get the username of the peer
            String[] newPeer = message.split(" ");
            // create a hashmap that stores a peer's IP address and port number
            HashMap<InetAddress, Integer> peerInfo = new HashMap<>();
            peerInfo.put(IPAddress, port);
            // store the peer's username with their info
            peers.put(newPeer[0], peerInfo);
        }
        // a peer is leaving the chat
        else if (message.contains("has left the chat")) {
            // get the username of the peer
            String[] peer = message.split(" ");
            // remove the username from the list of current peers
            peers.remove(peer[0]);
        }

        System.out.println(message);
        // send the message to all current peers
        sendMessage(message);
        // create a thread to handle sending the updated peer list
        new Thread(() -> {
            try {
                sendPeerList();
            } catch (IOException e) {}
        }).start();
    }

    // method that sends an updated peer list to all peers
    public void sendPeerList() throws IOException {
        // create a StringBuilder object to put together the peer information
        StringBuilder builder = new StringBuilder();
        // let the peers know that they are receiving updated peer information
        builder.append("peer info:\n");
        // loop through the usernames and their respective peer info
        for (HashMap.Entry<String, HashMap<InetAddress, Integer>> entry : peers.entrySet()) {
            // loop through the peer's IP address and port number
            for (HashMap.Entry<InetAddress, Integer> e : entry.getValue().entrySet()) {
                // format the message to include the peer's IP address and port number
                builder.append(e.getKey().getHostAddress() + " " + e.getValue() + "\n");
            }
        }
        // convert the peer list into a String
        String message = builder.toString();
        System.out.println(message);
        // send the message to all current peers
        sendMessage(message);
    }

    // method for main thread to receive messages from peers
    @Override
    public void run() {
        try {
            while (true) {
                receiveMessage();
            }
        } catch (Exception e) {}
    }

    // main method to run tracker program
    public static void main(String[] args) throws IOException {
        
        // initialize known port
        final int PORT = 5000;
        Tracker tracker = new Tracker(PORT);
        // create a thread to run this tracker
        Thread thread = new Thread(tracker);
        thread.start();

    }
    
}
