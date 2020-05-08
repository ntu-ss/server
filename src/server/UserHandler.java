package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import shared.utilities.FieldUpdateRequest;
import shared.utilities.FieldUpdateResponse;
import shared.utilities.Message;
import shared.utilities.StationDataRequest;
import shared.utilities.StationDataMessage;

public class UserHandler implements Runnable {
    
    private final Socket socket;
    private final Server server;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;
    private boolean isListening;
    private final String username;

    UserHandler(String username, Socket socket, Server server, ObjectInputStream objectInputStream, 
            ObjectOutputStream objectOutputStream) {
        this.socket = socket;
        this.server = server;
        this.objectInputStream = objectInputStream;
        this.objectOutputStream = objectOutputStream;
        isListening = true;
        this.username = username;
    }

    @Override
    public void run() {
        while(isReachable() && isListening) {
            try {
                Message message = (Message)readObject();
                System.out.println("Received message from user " + username + ". Processing...");
                processMessage(message);
            }
            catch(IOException e) {
                break;
            } 
            catch (ClassNotFoundException e) {
                System.out.println("Error in parsing message from client.");
            }
        }
        try {
            objectInputStream.close();
            objectOutputStream.close();
        } catch (IOException e) {
            System.out.println("Exception occured when trying to close object streams in StationHandler.");
        }
        server.removeUser(username);
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
        if(message instanceof FieldUpdateRequest) {
            sendFieldUpdate();
        }
        if(message instanceof StationDataRequest) {
            StationDataRequest request = (StationDataRequest)message;
            sendWeatherData(request.getId());
        }
        else {
            System.out.println("Message not recognised.");
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

    private void sendFieldUpdate() throws IOException {
        sendObject(new FieldUpdateResponse(server.getFieldCropType(), server.getFieldArea(), server.getActiveStations()));
    }

    private void sendWeatherData(String id) throws IOException {
        sendObject(new StationDataMessage(server.getPositionData(id), server.getTemperatureData(id), server.getHumidityData(id)));
    }
    
}
