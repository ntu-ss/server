package server;

import java.io.IOException;
import static java.lang.Integer.parseInt;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Collects weather station data and updates the user clients.
 */
public class Server {

    /**
     * Entry point for the server program.
     * 
     * Parses a port from the command line arguments and runs the server.
     * 
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        int port = parseInt(args[0]);
        Server server = new Server();
        server.run(port);
    }

    /**
     * Listens for connecting clients and spawns new handler threads for each.
     * 
     * @param port
     * @throws IOException 
     */
    private void run(int port) throws IOException {
        ServerSocket server = new ServerSocket(port);
        while(true) {
            System.out.println("Waiting for client...");
            Socket client = server.accept();
            System.out.println("Client " + client.getInetAddress() + " connected.");
            // TODO: Spawn client handler thread.
        }
    }
    
}
