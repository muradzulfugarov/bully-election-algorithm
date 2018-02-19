
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/* 
 * @author Murad
 */
public class Signaller {

    private Socket connection;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String serverIP;
    private int serverPort;
    private int nodeID;
    private int preCoordinatorID;

    public void checkLeader(String serverIP, int serverPort, int nodeID) throws IOException {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.nodeID = nodeID;
        preCoordinatorID = Main.leaderID;

        while (true) {
            try {
                connect(serverPort);
                setupStreams();
                sendMessage("checkOK");
                receiveData();
                closeConnection();

            } catch (IOException e) {
                startElection("The coordinator crashed", this.nodeID);
                break;
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Signaller.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }
    }

    private void connect(int coordinatorPort) {
        try {
            connection = new Socket(InetAddress.getByName(serverIP), coordinatorPort);
            System.out.println(nodeID + " connected to " + connection.getPort());
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch(ConnectException e) {            
        }
        catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void setupStreams() {
        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
        } catch (IOException ex) {
        }
    }

    public String receiveData() throws IOException {
        String message = "";
        try {
            while (true) {
                try {
                    //    System.out.println("Trying to read leader's response...");
                    message = (String) input.readObject();
                    System.out.println("Received response from leader: " + message);
                    if (message.equals("I AM OK")) {
                        break;
                    }
                } catch (SocketException e) {
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return message;
    }

    public void closeConnection() {
        try {
            if(input != null) input.close();
            if(output != null) output.close();
            if(connection != null) connection.close();

        } catch (IOException ex) {
//            ex.printStackTrace();
        }
    }

    public void sendMessage(String message) throws SocketException, IOException {
        output.writeObject(message);
        output.flush();
        System.out.println("CheckOK sent to leader from " + nodeID);

    }

    public void startElection(String reason, int nodeID) {
        // sending ELECTION message to nodes with higher id
        System.out.println("!!!!!" + reason + "!!!! \n Node-" + nodeID + " starting an election");

        Client selector = new Client("127.0.0.1", nodeID);
        Thread selectorThread = new Thread(selector);
        selectorThread.start();
        try {
            closeConnection();
            while (preCoordinatorID == Main.leaderID) {    // if election is not finished yet for other nodes, it waits
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Signaller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            int newCoordinatorPort = Integer.valueOf("987" + Main.leaderID);
            if (nodeID != Main.leaderID) {
                checkLeader(serverIP, newCoordinatorPort, nodeID);
            }
            
        } catch (IOException ex) {
           Logger.getLogger(Signaller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
