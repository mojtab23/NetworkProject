package chat;

import reliableudp.MyTask;
import reliableudp.ReliableUDPServer;

import java.io.*;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Mojtaba on 4/19/2015.
 */
public class ChatServer {

    public static final int CHAT_PORT = 55555;

    public static void main(String[] args) {

        Executor executor = Executors.newFixedThreadPool(10);
        ReliableUDPServer reliableUDPServer = null;
        try {
            reliableUDPServer = new ReliableUDPServer(CHAT_PORT);
            reliableUDPServer.setConnectionHandler(
                    connection -> executor.execute(
                            new MyTask(connection) {
                                @Override
                                public void run() {
                                    while (true) {

                                        try {
                                            System.out.println(connection);
                                            InputStream receive = connection.receive();
                                            System.out.println(readString(receive));
                                            String s = "welcome.";
                                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                            outputStream.write(s.getBytes());
                                            outputStream.flush();
                                            connection.send(outputStream);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            }));
            reliableUDPServer.accept();
            System.out.println("listening...");
        } catch (SocketException e) {
            e.printStackTrace();
        }


    }

    public static String readString(InputStream receive) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(receive));
            while ((line = br.readLine()) != null) {
                sb.append(line);
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
}
