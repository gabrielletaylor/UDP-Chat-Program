import java.io.*;
import java.net.*;
import java.util.*;

public class Tracker {

    private DatagramSocket tracker;
    private DatagramPacket receivePacket, sendPacket;
    private HashMap<String, HashMap<InetAddress, Integer>> peers = new HashMap<>();
    private final int BUFFER_SIZE = 1024;

    public Tracker(int port) {
        try {
            this.tracker = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws IOException {
        byte[] sendData = new byte[BUFFER_SIZE];
        sendData = message.getBytes();

        for (HashMap.Entry<String, HashMap<InetAddress, Integer>> entry : peers.entrySet()) {
            for (HashMap.Entry<InetAddress, Integer> e : entry.getValue().entrySet()) {
                InetAddress IPAddress = e.getKey();
                int port = e.getValue();
                sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                tracker.send(sendPacket);
            }
        }
    }

    public void receiveMessage() throws IOException {
        byte[] receiveData = new byte[BUFFER_SIZE];
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        tracker.receive(receivePacket);
        InetAddress IPAddress = receivePacket.getAddress();
        int port = receivePacket.getPort();

        String message = new String(receivePacket.getData());
        if (message.contains("has joined the chat")) {
            String[] newPeer = message.split(" ");
            HashMap<InetAddress, Integer> peerInfo = new HashMap<>();
            peerInfo.put(IPAddress, port);
            peers.put(newPeer[0], peerInfo);
        }
        else if (message.contains("has left the chat")) {
            String[] peer = message.split(" ");
            peers.remove(peer[0]);
        }
        System.out.println(message);
        sendMessage(message);
        sendPeerList();
    }

    public void sendPeerList() throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("peer info:\n");
        for (HashMap.Entry<String, HashMap<InetAddress, Integer>> entry : peers.entrySet()) {
            for (HashMap.Entry<InetAddress, Integer> e : entry.getValue().entrySet()) {
                builder.append(e.getKey().getHostAddress() + " " + e.getValue() + "\n");
            }
        }
        String message = builder.toString();
        System.out.println(message);
        sendMessage(message);
    }

    public static void main(String[] args) throws IOException {
        
        final int PORT = 5000;
        Tracker tracker = new Tracker(PORT);

        while (true) {
            tracker.receiveMessage();
        }
    }
    
}
