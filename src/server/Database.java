package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

/**
 * Interfaces with the text-based user authentication database.
 */
public class Database {
    
    private final File file;
    
    /**
     * Opens an existing one or creates a new file at the specified path and
     * writes the JSON schema to it if it's a new file.
     * 
     * @param path
     * @throws IOException 
     */
    public Database(String path) throws IOException {
        this.file = new File(path);
        if(file.createNewFile()) {
            writeSchema();
        }
    }
    
    /**
     * Adds a user by username and password to the database. If a user with the
     * same username already exists, returns false and makes no changes.
     * 
     * @param username
     * @param password
     * @return Boolean
     * @throws IOException
     * @throws ParseException 
     */
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
    
    /**
     * Checks if a user is present in the database and whether their password 
     * is correct.
     * 
     * @param username
     * @param password
     * @return Boolean
     * @throws IOException
     * @throws ParseException
     */
    public boolean authenticateUser(String username, String password) throws IOException, ParseException {
        String string = readFile();
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject)parser.parse(string);
        return object.containsKey(username) && object.get(username).equals(password);
    }

    /**
     * Writes an empty JSON object to the file.
     * 
     * @param fileWriter
     * @throws IOException 
     */
    private void writeSchema() throws IOException {
        JSONObject object = new JSONObject();
        writeJSON(object);
    }

    /**
     * Reads the database to a string and returns it.
     * 
     * @return String
     * @throws IOException 
     */
    private String readFile() throws IOException {
        String content = "";
        content = new String(Files.readAllBytes(file.toPath()));
        return content;
    }

    /**
     * Writes the string representation of a JSON object to the database.
     * 
     * @param object
     * @throws IOException 
     */
    private void writeJSON(JSONObject object) throws IOException {
        FileWriter fileWriter;
        fileWriter = new FileWriter(file, false);
        String string = object.toJSONString();
        fileWriter.write(string);
        fileWriter.flush();
        fileWriter.close();
    }
}
