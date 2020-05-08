package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import shared.utilities.Message;
import shared.utilities.StationDataMessage;

public class StationHandler implements Runnable {
    
    private final Socket socket;
    private final Server server;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;
    private boolean isListening;
    private String previousPosition;
    private int previousHumidity;
    private int previousTemperature;
    private final String id;

    StationHandler(String id, Socket socket, Server server, ObjectInputStream objectInputStream, 
            ObjectOutputStream objectOutputStream) throws IOException {
        this.socket = socket;
        this.server = server;
        this.objectInputStream = objectInputStream;
        this.objectOutputStream = objectOutputStream;
        isListening = true;
        previousPosition = "";
        previousHumidity = 0;
        previousTemperature = 0;
        this.id = id;
    }

    @Override
    public void run() {
        while(isReachable() && isListening) {
            try {
                Message message = (Message)readObject();
                System.out.println("Received message from station " + id + ". Processing...");
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
        server.removeStation(id);
    }
    
    private boolean isReachable() {
        try {
            return this.socket.getInetAddress().isReachable(300);
        }
        catch(IOException e) {
            return false;
        }
    }

    private void processMessage(Message message) {
        if(message instanceof StationDataMessage) {
            recordStationData((StationDataMessage)message);
        }
        else {
            System.out.println("Message not recognised.");
        }
    }

    private void recordStationData(StationDataMessage stationDataMessage) {
        previousPosition = stationDataMessage.getPosition();
        previousHumidity = stationDataMessage.getHumidity();
        previousTemperature = stationDataMessage.getTemperature();
        System.out.println("Recorded weather data from station " + id + ":" + 
                "\nPosition: " + previousPosition + 
                "\nHumidity: " + previousHumidity + "%" +
                "\nTemperature: " + previousTemperature + " C");
    }
    
    private Object readObject() throws IOException, ClassNotFoundException {
        Object object = objectInputStream.readObject();
        return object;
    }
    
    public String getPosition() {
        return previousPosition;
    }
    
    public int getHumidity() {
        return previousHumidity;
    }
    
    public int getTemperature() {
        return previousTemperature;
    }
}
