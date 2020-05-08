package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.json.simple.parser.ParseException;

import shared.utilities.ClientAuthenticationResponse;
import shared.utilities.Message;
import shared.utilities.StationRegistrationRequest;
import shared.utilities.UserAuthenticationRequest;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Server server;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;
    private boolean isListening;
    
    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        OutputStream outputStream = socket.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
        InputStream inputStream = socket.getInputStream();
        objectInputStream = new ObjectInputStream(inputStream);
        isListening = true;
    }
    
    @Override
    public void run() {
        while(isReachable() && isListening) {
            try {
                Message message = (Message)readObject();
                System.out.println("Received message from " + socket.getInetAddress() + ". Processing...");
                processMessage(message);
            }
            catch(IOException e) {
                System.out.println("Client disconnected before authenticating.");
                break;
            } 
            catch (ClassNotFoundException e) {
                System.out.println("Error in parsing message from client.");
            }
        }
    }

    private boolean isReachable() {
        try {
            return this.socket.getInetAddress().isReachable(300);
        }
        catch(IOException e) {
            return false;
        }
    }

    private void processMessage(Message message) throws IOException {
        if(message instanceof UserAuthenticationRequest) {
            authenticateUser((UserAuthenticationRequest)message);
        }
        else if(message instanceof StationRegistrationRequest) {
            registerStation((StationRegistrationRequest)message);
        }
        else {
            System.out.println("Message not recognised.");
        }
    }

    private void authenticateUser(UserAuthenticationRequest message) throws IOException {
        try {
            if(server.authenticateUser(message.getUsername(), message.getPassword())) {
                sendObject(new ClientAuthenticationResponse(true));
                System.out.println("authenticated user");
                server.addActiveUser(message.getUsername(), socket, objectInputStream, objectOutputStream);
                isListening = false;
            }
            else {
                sendObject(new ClientAuthenticationResponse(false));
            }
        } 
        catch (IOException | ParseException e) {
            sendObject(new ClientAuthenticationResponse(false));
        }
    }

    private void sendObject(Object object) throws IOException {
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
    }

    private Object readObject() throws IOException, ClassNotFoundException {
        Object object = objectInputStream.readObject();
        return object;
    }

    private void registerStation(StationRegistrationRequest message) throws IOException {
        sendObject(new ClientAuthenticationResponse(true));
        server.registerStation(message.getId(), socket, objectInputStream, objectOutputStream);
        isListening = false;
    }
}
