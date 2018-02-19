
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Murad
 */
public class Server {

    private static ServerSocket server;
    private static Socket connection;
    private static ObjectOutputStream output;
    private static ObjectInputStream input;
    private int port;
    private int nodeID;

    public Server(int port, int nodeID) {
        this.port = port;
        this.nodeID = nodeID;
    }

    public void start() {
        try {
            server = new ServerSocket(port);
            while (true) {
                System.out.println("Node-" + nodeID + ": is waiting for connection ...");
                connection = server.accept();
                output = new ObjectOutputStream(connection.getOutputStream());
                input = new ObjectInputStream(connection.getInputStream());
                System.out.println("Node-" + nodeID + ": connected to " + connection.getPort());
                String request;
                try {
                    request = (String) input.readObject();
                    if (!request.equals("end")) {
                        if (request.equals("checkOK")) {
                            System.out.println("\n Node-" + nodeID + " received ping from " + connection.getPort());
                            output.writeObject("I AM OK");
                            System.out.println("Leader sent 'I AM OK' to " + connection.getPort());
                        } else if (request.equals("ELECTION")) {
                            System.out.println("\n Node-" + nodeID + " received ELECTION from " + connection.getPort());
                            output.writeObject("OK");
                            if (nodeID != Main.leaderID) {
                                startElection();
                            }
                        } else if (request.substring(0, 8).equals("VICTORY-")) {
                            int newCoordinatorID = Integer.valueOf(request.substring(8));
                            output.writeObject("GOT NEW COORDINATOR");
                            System.out.println("*********  New coordinator is Node-" + newCoordinatorID + "  *********");
                            Main.leaderID = newCoordinatorID;
                        }
                    }
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
                output.close();
                input.close();
                connection.close();
            }
        } catch (IOException e) {
           // e.printStackTrace();
        }
    }

    private void startElection() throws IOException {
        // sending ELECTION message to nodes with higher id    
        System.out.println(nodeID + " starting ELECTION after OK...");
        Client selector = new Client("127.0.0.1", nodeID);
        Thread selectorThread = new Thread(selector);
        selectorThread.start();

    }

}
