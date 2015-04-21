package chat;

import reliableudp.Connection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mojtaba on 4/19/2015.
 */
public class User {

    public List<String> getChats() {
        return chats;
    }

    private List<String> chats = new ArrayList<>();
    private boolean isOnline;
    private Connection connection;


    public User() {
        new User(true);
    }

    public User(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public User(Connection connection) {
        this.connection = connection;
    }

    public void addChat(String text) {
        chats.add(text);
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {


        this.isOnline = isOnline;
    }

    public Connection getConnection() {
        return connection;
    }
}
