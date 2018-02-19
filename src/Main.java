
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    static int nodeID = -1;
    static int leaderID = 5;
    static int resumedNodeId;

    public static void assignID() {
        try {
            Scanner in = new Scanner(new File("ids.txt"));
            int number = in.nextInt();
            if (number == 0) {
                in.close();
                return;
            }
            nodeID = number;
            in.close();
            PrintWriter out = new PrintWriter(new File("ids.txt"));
            out.println(number - 1);
            out.flush();
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) throws InterruptedException {
        resumedNodeId = -1;
        if (args != null && args.length > 0) {
            resumedNodeId = Integer.valueOf(args[0]);
            nodeID = resumedNodeId;
            leaderID = 0;
            System.out.println(resumedNodeId);
        } else {
            assignID();
            System.out.println("*** Your leader is Node-" + leaderID + " ***");
        }

        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int port = Integer.valueOf("987" + nodeID);
                Server server = new Server(port, nodeID);
                server.start();
            }
        });

        serverThread.start();

        if (leaderID != nodeID) {
            Thread signallerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Signaller signaller = new Signaller();
                    try {
                        int leaderPort = Integer.valueOf("987" + leaderID);
                        if (resumedNodeId > 0) {
                            signaller.closeConnection();
                            signaller.startElection("A node is resumed", nodeID);
                        }
                        signaller.checkLeader("127.0.0.1", leaderPort, nodeID);
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

            signallerThread.start();
        }
    }
}
