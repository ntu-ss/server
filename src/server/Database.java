package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

public class Database {
    
    private final File file;

    public Database(String path) throws IOException {
        this.file = new File(path);
        if(file.createNewFile()) {
            writeSchema();
        }
    }

    public boolean addUser(String username, String password) throws IOException, ParseException {
        String string = readFile();
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject)parser.parse(string);
        if(object.containsKey(username)) {
            return false;
        }
        else {
            object.put(username, password);
            writeJSON(object);
            return true;
        }
    }

    public boolean authenticateUser(String username, String password) throws IOException, ParseException {
        String string = readFile();
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject)parser.parse(string);
        return object.containsKey(username) && object.get(username).equals(password);
    }

    private void writeSchema() throws IOException {
        JSONObject object = new JSONObject();
        writeJSON(object);
    }

    private String readFile() throws IOException {
        String content = "";
        content = new String(Files.readAllBytes(file.toPath()));
        return content;
    }

    private void writeJSON(JSONObject object) throws IOException {
        FileWriter fileWriter;
        fileWriter = new FileWriter(file, false);
        String string = object.toJSONString();
        fileWriter.write(string);
        fileWriter.flush();
        fileWriter.close();
    }

    void addDummyUsers() {
        try {
            addUser("test", "test");
            addUser("test1", "test1");
            addUser("test2", "test2");
            addUser("test3", "test3");
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }
}
