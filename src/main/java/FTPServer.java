import reliableudp.MyTask;
import reliableudp.ReliableUDPServer;

import java.io.*;
import java.lang.reflect.Array;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by ArcxX on 4/19/2015.
 */
public class FTPServer {


    static int portNumber;
    static String curDir;
    ReliableUDPServer reliableUDPServer = null;
    Executor executor;
    private DatagramSocket socket = null;

    public static void main(String[] args) {
        FTPServer server = new FTPServer();

        server.initialValues();

        if (server.startSever()) {
            System.out.println("Server Start Successfully.");
        } else {
            System.out.println("Cannot Start Server.");
        }

        server.Waiting();


    }

    private static String list() {
        String result = String.format(("File And Directories in : " + "\"" + curDir + "\"" + "\n\n"));
        File folder = new File(curDir);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String s = String.format("File | %-15s %15s %n", listOfFiles[i].getName(), listOfFiles[i].length());
                result = result + s;
            }
        }

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isDirectory()) {
                String s = String.format("Dir  | %-15s %15s %n", listOfFiles[i].getName(), listOfFiles[i].length());
                result = result + s;
            }
        }
        return result;
    }

    private static String curdir() {
        return "> " + curDir;
    }

    private static String cd(String path) {

        boolean isRelative = false;

        if (path.equals("..")) {

            if (curDir.contains("\\")) {
                curDir = curDir.substring(0, curDir.lastIndexOf("\\"));
            } else if (curDir.contains("/")) {
                curDir = curDir.substring(0, curDir.lastIndexOf("/"));
            } else if (curDir.contains(":")) {
                curDir = System.getenv("WINDIR");
                cd("..");
            }

            return curdir();
        }

        if (path.equals("~")) {
            curDir = System.getenv("WINDIR");
            cd("..");
            return curdir();
        }

        if (path.contains(":")) {
            isRelative = false;
            curDir = path;
            return curdir();

        } else {

            // ? complete this

        }

        curDir = path;
        return curdir();
    }

    private void Waiting() {

        reliableUDPServer.setConnectionHandler(
                connection -> executor.execute(
                        new MyTask(connection) {
                            @Override
                            public void run() {
                                while (true) {

                                    try {
                                        System.out.println(connection);
                                        InputStream receive = connection.receive();

                                        int read = 0;
                                        byte[] bytes = new byte[1024];


                                        while ((read = receive.read(bytes)) != -1) {

                                            String req = "" + (char) Array.getByte(bytes, 0) + (char) Array.getByte(bytes, 1) + (char) Array.getByte(bytes, 2);
                                            if (req.equals("CMD")) {
                                                String FullMSG = new String(bytes);
                                                String MSG = FullMSG.substring(4, FullMSG.indexOf("*@*"));
                                                System.out.println("Received : '" + MSG + "'");

                                                String reply = checkStatus(MSG);
                                                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                                outputStream.write(reply.getBytes());
                                                outputStream.flush();
                                                connection.send(outputStream);

                                            } else {

                                                if (req.substring(0, 3).equals("FIG")) {

                                                    String FullMSG = new String(bytes);
                                                    String MSG = FullMSG.substring(4, FullMSG.indexOf("*@*"));
                                                    String fileName = MSG;
                                                    String path = curDir + "\\" + fileName;
                                                    FileInputStream fis = null;

                                                    try {
                                                        File file = new File(path);
                                                        System.out.println(file.length());
                                                        fis = new FileInputStream(file);
                                                    } catch (Exception e) {
                                                        System.out.println(e);
                                                    }

                                                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                                    byte[] buf = new byte[1024];
                                                    try {
                                                        for (int readNum; (readNum = fis.read(buf)) != -1; ) {
                                                            bos.write(buf, 0, readNum); //no doubt here is 0
                                                        }
                                                    } catch (IOException ex) {
                                                    }
                                                    System.out.println(bos.size());
                                                    connection.send(bos);
                                                    System.out.println("Successfully Send data to client");

                                                } else if (req.substring(0, 3).equals("FIS")) {
                                                    //send
                                                    System.out.println("Fis block");
                                                    String FullMSG = new String(bytes);
                                                    String MSG = FullMSG.substring(4, FullMSG.indexOf("*@*"));
                                                    String fileName = MSG.substring(MSG.lastIndexOf("\\") + 1);
                                                    String path = curDir + "\\" + fileName;
                                                    System.out.println("p is : " + path);
                                                    try {
                                                        try {
                                                            receive = connection.receive();

                                                            OutputStream fileOutputStream = null;
                                                            File file = new File(path);
                                                            fileOutputStream = new FileOutputStream(file);

                                                            while ((read = receive.read(bytes)) != -1) {
                                                                fileOutputStream.write(bytes, 0, read);

                                                            }
                                                            System.out.println("File Successfully Created in " + fileName);

                                                        } catch (Exception e) {
                                                            System.out.println("Cannot Send." + e);
                                                        } finally {

                                                        }

                                                    } catch (Exception e) {
                                                        System.out.println("Cannot Get File From Client" + e);
                                                    }
                                                }

                                            }

                                        }
                                        System.out.println("Done");

                                    } catch (Exception e) {

                                    }
                                }
                            }

                        }));
        reliableUDPServer.accept();
        System.out.println("Server is running ...");

    }

    private boolean startSever() {
        try {
            this.reliableUDPServer = new ReliableUDPServer(portNumber);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void initialValues() {
        this.executor = Executors.newFixedThreadPool(10);
        this.portNumber = 50000;
        curDir = System.getProperty("user.dir");
    }

    private String ftp() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName().toString();
            return "You Successfully Connected To : " + hostName;
        } catch (Exception ex) {
            return "You Successfully Connect To FTP Server.";
        }
    }

    private String checkStatus(String msg) {

        if (msg.equalsIgnoreCase("Error")) {
            msg = "Some issue happened while packing the data @ client side";
            System.out.println("Send \"" + msg + "\" To Client " + "###.");
            return msg;
        }

        if (msg.equalsIgnoreCase("list")) {
            msg = list();
            System.out.println("Send \n" + msg + " To Client " + "###.");
            return msg;

        }

        if (msg.contains("ftp")) {
            msg = ftp();
            System.out.println("Send " + msg + " To Client " + "###.");
            return msg;

        }

        if (msg.contains("curdir")) {
            msg = curdir();
            System.out.println("Send " + msg + " To Client " + "###.");
            return msg;
        }


        if (msg.substring(0, 2).equalsIgnoreCase("cd")) {
            msg = cd(msg.substring(3, msg.length()));
            System.out.println("Send " + msg + " To Client " + "###.");
            return msg;
        }


        return "Error Happen";
    }


}
