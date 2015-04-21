package chat;

import reliableudp.MyTask;

import java.io.*;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Mojtaba on 4/19/2015.
 */
public class ChatServer {

    public static final int CHAT_PORT = 55555;

    public static void main(String[] args) {

        Executor executor = Executors.newFixedThreadPool(10);
        ReliableUDPChatServer reliableUDPServer = null;
        try {
            reliableUDPServer = new ReliableUDPChatServer(CHAT_PORT);
            reliableUDPServer.setConnectionHandler(
                    (connection, users, userName) -> executor.execute(
                            new MyTask(connection) {
                                @Override
                                public void run() {
                                    while (true) {

                                        try {
                                            System.out.println(connection);
                                            InputStream receive = connection.receive();
                                            String request = readString(receive);
                                            System.out.println(request);
                                            redirect(request);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }

                                private void redirect(String request) {
                                    Thread thread = new Thread(() -> {
                                        String header = request.substring(0, request.indexOf('\n'));
                                        if (header.equalsIgnoreCase("list")) {
                                            sendUserList(users);
                                        } else if (header.equalsIgnoreCase("chat")) {
                                            String body = request.substring(request.indexOf('\n'));
                                            String userName = body.substring(0, request.indexOf('\n'));
                                            sendToUser(userName, body.substring(request.indexOf('\n')));
                                        }
                                    });
                                    thread.setDaemon(true);
                                    thread.start();

                                }

                                private void sendToUser(String receiver, String chat) {
                                    User user = users.get(receiver);
                                    if (user != null) {
                                        try {
                                            user.addChat(chat);
                                            StringBuilder builder = new StringBuilder();
                                            builder.append("chat\n");
                                            builder.append(userName);
                                            builder.append('\n');
                                            builder.append(chat);
                                            String response = builder.toString();
                                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                            outputStream.write(response.getBytes());
                                            outputStream.flush();
                                            user.getConnection().send(outputStream);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                private void sendUserList(ConcurrentHashMap<String, User> users) {
                                    try {
                                        StringBuilder builder = new StringBuilder();
                                        builder.append("list\n");
                                        users.entrySet().forEach(stringUserEntry -> {
                                            builder.append(stringUserEntry.getKey());
                                            builder.append('\n');
                                        });
                                        String response = builder.toString();
                                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                        outputStream.write(response.getBytes());
                                        outputStream.flush();
                                        connection.send(outputStream);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                public String readString(InputStream receive) {
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
                            }));
            reliableUDPServer.accept();
            System.out.println("listening...");
        } catch (SocketException e) {
            e.printStackTrace();
        }


    }


}
