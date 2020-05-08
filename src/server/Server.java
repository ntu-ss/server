package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.simple.parser.ParseException;

public class Server {

    private final ConcurrentHashMap<String, UserHandler> activeUsers;
    private final ConcurrentHashMap<String, StationHandler> activeStations;
    private final ExecutorService executor;
    private final Database database;
    private final ServerUI serverUI;
    
    public Server() throws IOException {
        activeUsers = new ConcurrentHashMap<>();
        activeStations = new ConcurrentHashMap<>();
        database = new Database("database.txt");
        executor = Executors.newFixedThreadPool(10);
        this.serverUI = new ServerUI(this);
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.run(8080);
    }

    public void run(int port) throws IOException {
        ServerSocket server = new ServerSocket(port);
        database.addDummyUsers();
        Thread uiThread = new Thread(serverUI);
        uiThread.start();
        while(true) {
            System.out.println("Waiting for client...");
            Socket client = server.accept();
            System.out.println("Client " + client.getInetAddress() + " connected.");
            executor.submit(new ClientHandler(client, this));
        }
    }

    public void addActiveUser(String username, Socket socket, ObjectInputStream objectInputStream, 
            ObjectOutputStream objectOutputStream) {
        System.out.println("adding user");
        UserHandler userHandler = new UserHandler(username, socket, this, objectInputStream, objectOutputStream);
        System.out.println("user created");
        activeUsers.put(username, userHandler);
        System.out.println("user put");
        executor.submit(userHandler);
        updateUserList();
        System.out.println("New authenticated user: " + username);
    }

    public boolean authenticateUser(String username, String password) throws IOException, ParseException {
        return database.authenticateUser(username, password);
    }

    public void registerStation(String id, Socket socket, ObjectInputStream objectInputStream, 
            ObjectOutputStream objectOutputStream) throws IOException {
        StationHandler stationHandler = new StationHandler(id, socket, this, objectInputStream, objectOutputStream);
        activeStations.put(id, stationHandler);
        executor.submit(stationHandler);
        updateStationList();
        System.out.println("New registered station: " + id);
    }
    
    public String getFieldCropType() {
        return "Corn";
    }
    
    public int getFieldArea() {
        return 1600;
    }

    String[] getActiveStations() {
        String[] stations = new String[activeStations.size()];
        for (int i = 0; i < activeStations.size(); i++) {
            stations[i] = (String)activeStations.keySet().toArray()[i];
        }
        return stations;
    }
    
    String[] getActiveUsers() {
        String[] users = new String[activeUsers.size()];
        for (int i = 0; i < activeUsers.size(); i++) {
            users[i] = (String)activeUsers.keySet().toArray()[i];
        }
        return users;
    }

    void removeStation(String id) {
        activeStations.remove(id);
        updateStationList();
        System.out.println("Station " + id + " disconnected.");
    }

    void removeUser(String username) {
        activeUsers.remove(username);
        updateUserList();
        System.out.println("User " + username + " disconnected.");
    }
    
    void updateStationList() {
        serverUI.updateStations(getActiveStations());
    }

    void updateUserList() {
        serverUI.updateUsers(getActiveUsers());
    }
    
    String getPositionData(String stationID) {
        return activeStations.get(stationID).getPosition();
    }
    
    int getTemperatureData(String stationID) {
        return activeStations.get(stationID).getTemperature();
    }
    
    int getHumidityData(String stationID) {
        return activeStations.get(stationID).getHumidity();
    }
}
