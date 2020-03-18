package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.json.simple.parser.ParseException;

import shared.utilities.AuthenticationResponseMessage;
import shared.utilities.Message;
import shared.utilities.UserAuthenticationMessage;

/**
 * Gets spawned for each new client connection and is responsible for 
 * communicating with it.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Database database;
    private final Server server;
    
    /**
     * Constructs a Client Handler and sets its socket, inputStream and 
     * outputStream.
     * 
     * @param socket
     * @param database
     * @param server
     * @throws IOException 
     */
    public ClientHandler(Socket socket, Database database, Server server) throws IOException {
        this.socket = socket;
        this.database = database;
        this.server = server;
    }
    
    /**
     * Continuously reads messages from the client while it is reachable.
     */
    @Override
    public void run() {
        while(isReachable()) {
            try {
                Message message = (Message)readObject();
                processMessage(message);
            }
            catch(IOException e) {
                // No message received, continue listening.
            } 
            catch (ClassNotFoundException e) {
                System.out.println("Error in parsing message from client.");
            }
        }
    }

    /**
     * Checks if the client is reachable.
     * 
     * @return Boolean
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
    private void processMessage(Message message) throws IOException {
        if(message instanceof UserAuthenticationMessage) {
            authenticateUser((UserAuthenticationMessage)message);
        }
        //TODO: Add weather station message processing.
    }

    /**
     * Check is the authentication message is valid and send a response to the
     * user. If accepted, add this handler to the server's active users.
     * 
     * @param message
     * @throws IOException
     */
    private void authenticateUser(UserAuthenticationMessage message) throws IOException {
        try {
            if(database.authenticateUser(message.getUsername(), message.getPassword())) {
                sendObject(new AuthenticationResponseMessage(true));
                server.addActiveUser(message.getUsername(), this);
            }
            else {
                sendObject(new AuthenticationResponseMessage(false));
            }
        } catch (IOException | ParseException e) {
            sendObject(new AuthenticationResponseMessage(false));
        }
    }
    
    /**
     * Send an object to the client.
     * 
     * @param object
     * @throws IOException 
     */
    private void sendObject(Object object) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(object);
        outputStream.flush();
        outputStream.close();
    }
    
    /**
     * Reads an object from the client.
     * 
     * @return Object
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    private Object readObject() throws IOException, ClassNotFoundException {
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        Object object = inputStream.readObject();
        inputStream.close();
        return object;
    }

}
