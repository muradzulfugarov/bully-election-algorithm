
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Murad
 */
public class Client implements Runnable {

    private Socket connection;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String serverIP;
    private int nodeID;
    private int OKs = 0;

    public Client(String serverIP, int nodeID) {
        this.serverIP = serverIP;
        this.nodeID = nodeID;
    }

    public String start(int port, String message) {
        String response = "";
        try {
            if (connect(port)) {
                setupStreams();
                sendMessage(message);
                communicate();
                closeConnection();
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

    private boolean connect(int port) {
        boolean connected = false;
        System.out.println("Node-" + nodeID + " trying to connect...");
        try {
            System.out.println(serverIP + " " + port);
            connection = new Socket(InetAddress.getByName(serverIP), port);
            System.out.println(nodeID + " connected to " + connection.getInetAddress().getHostName());
            connected = true;
        } catch (ConnectException e) {
            System.out.println("Node at port " + port + " is not active ");
        } catch (IOException e) {
            System.out.println("Node at port " + port + " is not active inside IO");
        } finally {
            return connected;
        }
    }

    private void setupStreams() {
        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            System.err.println("Streams are ready");
        } catch (IOException ex) {
            System.out.println("ERROR -- Stream corruption error");
        }
    }

    public boolean communicate() throws IOException {
        boolean cont = true;
        String message = "";
        try {
            while (true) {
                if (input != null) {
                    try {
                        message = (String) input.readObject();

                        if (message.equals("OK")) {
                            OKs++;
                            System.out.println("Received election response from " + connection.getPort() + " " + message.toUpperCase());
                            break;
                        } else if (message.equals("GOT NEW COORDINATOR")) {
                            break;
                        }
                    } catch (SocketException e) {
                        break;
                    }
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cont;

    }

    public void sendMessage(String message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
           // e.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void run() {
        for (int i = nodeID + 1; i <= Main.leaderID; i++) {
            int higherPort = Integer.valueOf("987" + i);
            start(higherPort, "ELECTION");
        }
        if (OKs == 0) {
            System.out.println("--------- Leader " + nodeID + " announced victory ---------------------  " + nodeID);            
            announceVictory(nodeID);
            Main.leaderID = nodeID;
        }
    }

    private void announceVictory(int nodeID) {
        for (int i = 1; i <= 5; i++) {
            int port = Integer.valueOf("987" + i);
            start(port, "VICTORY-" + nodeID);
        }
    }
}
