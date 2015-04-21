package chat;

import reliableudp.Connection;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Mojtaba on 4/20/2015.
 */
public interface ConnectionHandlerChat {
    void handleConnection(Connection connection, ConcurrentHashMap<String, User> users, String userName);
}
