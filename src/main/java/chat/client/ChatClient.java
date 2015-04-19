package chat.client;

import chat.ChatServer;
import chat.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * Created by Mojtaba on 4/19/2015.
 */
public class ChatClient extends Application {

    private HashMap<String, User> users = new HashMap<>();
    private String userName;


    public static void main(String[] args) {
        launch(args);
    }


    public void connect(String userName) {
        try {
            ReliableUDPChatClient client = new ReliableUDPChatClient
                    (InetAddress.getByName("127.0.0.1"), ChatServer.CHAT_PORT);
            client.connect(userName);
            String s = "salam";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(s.getBytes());
            outputStream.flush();
            client.send(outputStream);
            InputStream receive = client.receive();


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void start(Stage primaryStage) throws Exception {


        Parent root = FXMLLoader.load(getClass().getResource("/chat-gui.fxml"));

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
