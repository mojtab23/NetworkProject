package chat.client;

import chat.ChatServer;
import chat.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mojtaba on 4/19/2015.
 */


public class Controller {


    @FXML
    public TextField userName;
    @FXML
    public TextField textInput;
    @FXML
    public Button connectButton;
    @FXML
    public Label status;
    @FXML
    public ComboBox<String> users;
    @FXML
    public ListView<String> history = new ListView<>();


    private ReliableUDPChatClient client;
    private ObservableMap<String, User> userMap;
    private ObservableList<String> entries;


    @FXML
    public void initialize() {
        userMap = FXCollections.observableHashMap();
        updateComboBox();
    }

    private void updateComboBox() {
//        entries = FXCollections.observableArrayList(userMap.keySet());
        entries = FXCollections.observableArrayList(
                "chocolate", "salmon", "gold", "coral", "darkorchid",
                "darkgoldenrod", "lightsalmon", "black", "rosybrown", "blue",
                "blueviolet", "brown");
        users = new ComboBox<>(entries);
        users.setCellFactory(list -> new ColorRectCell());
        users.getSelectionModel().selectFirst(); // Select first as default
        users.getSelectionModel().selectedItemProperty().addListener((ov, old_val, new_val) -> {
            history.setItems(FXCollections.observableArrayList(userMap.get(new_val).getChats()));
        });
    }

    public void connect() {
        Thread thread = new Thread(() -> {
            try {
                client = new ReliableUDPChatClient(InetAddress.getByName("127.0.0.1"), ChatServer.CHAT_PORT);

                if (client.connect(userName.getText().trim())) {
                    Platform.runLater(() -> {
                        connectButton.setDisable(true);
                        userName.setDisable(true);
                        status.setText("INFO: Connected...");
                    });
                    listen();

                    //list userMap
                    refreshUsers();
                } else {
                    Platform.runLater(() -> status.setText("ERROR: user name rejected. use another name..."));
                }


            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void listen() {
        Thread thread = new Thread(() -> {
            String request;
            while (true) {
                try {
                    request = createString(client.receive());
                    //todo debug
                    System.out.println("New Request: {\n " + request + " }");
                    redirect(request);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();


    }

    private void redirect(String request) {
        Thread thread = new Thread(() -> {
//            System.out.println(request);
            String header = request.substring(0, request.indexOf('\n'));
            if (header.equalsIgnoreCase("list")) {
                String body = request.substring(request.indexOf('\n'));
                updateUsers(body);
            } else if (header.equalsIgnoreCase("chat")) {
                String body = request.substring(request.indexOf('\n'));
                userMap.get(body.substring(0, request.indexOf('\n')))
                        .addChat(body.substring(request.indexOf('\n')));
            } else if (header.equalsIgnoreCase("error")) {
                String body = request.substring(request.indexOf('\n'));
                status.setText(body);
            }
        });
        thread.setDaemon(true);
        thread.start();

    }

    private void updateUsers(String body) {
//        System.out.println(userMap.size());
        String[] userNames = body.trim().split("\n");
        userMap.values().forEach(user -> user.setIsOnline(false));
        for (String name : userNames) {
            User user = userMap.get(name);
            if (user != null) user.setIsOnline(true);
            else userMap.put(name, new User(true));
        }
        List<String> rm = new ArrayList<>();
        userMap.forEach((s, user) -> {
            if (!user.isOnline()) rm.add(s);
        });
        for (String s : rm) userMap.remove(s);
//        System.out.println(userMap.size());
        updateComboBox();
    }


    private String createString(InputStream receive) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
//        int linNO = 0;
        String line;
        try {

            br = new BufferedReader(new InputStreamReader(receive));
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
//                linNO++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public void refreshUsers() {
        try {
            String s = "list\n";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(s.getBytes());
            outputStream.flush();
            client.send(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send() {
        Thread thread = new Thread(() -> {
            try {
                String s = "chat\n";
                String userName = users.getSelectionModel().getSelectedItem();
                s = s.concat(userName + "\n");
                String chat = textInput.getText();
                s = s.concat(chat);


                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(s.getBytes());
                outputStream.flush();
                Platform.runLater(() -> {
                    try {
                        client.send(outputStream);
                        userMap.get(userName).addChat(chat);
                        textInput.clear();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    static class ColorRectCell extends ListCell<String> {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            Rectangle rect = new Rectangle(100, 20);
            if (item != null) {
                rect.setFill(Color.web(item));
                setGraphic(rect);
            }
        }
    }

}
