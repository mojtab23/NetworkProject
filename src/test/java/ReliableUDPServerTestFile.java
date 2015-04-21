
import reliableudp.MyTask;
import reliableudp.ReliableUDPServer;

import java.io.*;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Mojtaba on 4/19/2015.
 */
public class ReliableUDPServerTestFile {

    public static void main(String[] args) {
        Executor executor = Executors.newFixedThreadPool(10);
        ReliableUDPServer reliableUDPServer = null;
        try {
            reliableUDPServer = new ReliableUDPServer(10024);
            reliableUDPServer.setConnectionHandler(
                    connection -> executor.execute(
                            new MyTask(connection) {
                                @Override
                                public void run() {
                                    while (true) {

                                        try {
                                            System.out.println(connection);
                                            InputStream receive = connection.receive();
                                            OutputStream outputStream = null;

                                            try {
                                                // write the inputStream to a FileOutputStream
                                                outputStream =
                                                        new FileOutputStream(new File("E:\\tamrin\\Intellij\\NetworkProject\\src\\test\\java\\plain2-1.jpg"));

                                                int read = 0;
                                                byte[] bytes = new byte[1024];

                                                while ((read = receive.read(bytes)) != -1) {
                                                    outputStream.write(bytes, 0, read);
                                                }

                                                System.out.println("Done!");

                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } finally {
                                                if (receive != null) {
                                                    try {
                                                        receive.close();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                if (outputStream != null) {
                                                    try {
                                                        // outputStream.flush();
                                                        outputStream.close();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                }
                                            }
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


}
