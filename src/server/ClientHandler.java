package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import shared.utilities.Message;

/**
 * Gets spawned for each new client connection and is responsible for 
 * communicating with it.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    
    /**
     * Constructs a Client Handler and sets its socket, inputStream and 
     * outputStream.
     * 
     * @param socket
     * @throws IOException 
     */
    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = new ObjectInputStream(socket.getInputStream());
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    }
    
    /**
     * Continuously reads messages from the client while it is reachable.
     */
    @Override
    public void run() {
        while(isReachable()) {
            try {
                Message message = (Message) inputStream.readObject();
                processMessage(message);
            }
            catch(IOException e) {
                System.out.println("Error in receiving message from client.");
                e.printStackTrace();
            } 
            catch (ClassNotFoundException e) {
                System.out.println("Error in parsing message from client.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks if the client is reachable.
     * 
     * @return 
     */
    private boolean isReachable() {
        try {
            return this.socket.getInetAddress().isReachable(300);
        }
        catch(IOException e) {
            return false;
        }
    }

    /**
     * Determine the type of message and process it accordingly.
     * 
     * @param message 
     */
    private void processMessage(Message message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
