import reliableudp.ReliableUDPClient;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Created by ArcxX on 4/19/2015.
 */
public class FTPClient {


    //65508 bytes, is the maximum amount of data you can send in a single UDP packet
    final static int MAXIMUM_SIZE_OF_DATA = 65500;
    static int portNumber = 50000;
    static int timeOut = 3000;
    private static String hostName = "localHost";
    ReliableUDPClient client;
    private DatagramSocket socket = null;
    private String sourceFilePath = "D:/x.txt";
    private String destinationPath = "D:/y.txt";

    public static void main(String[] args) {

        FTPClient ftpClient = new FTPClient();
        ftpClient.MainMenu("help");
        ftpClient.getCmd();

    }

    private static String readString(InputStream receive) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        int linNO = 0;
        String line;
        try {

            br = new BufferedReader(new InputStreamReader(receive));
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
                linNO++;
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

    private void MainMenu(String parameter) {

        parameter = parameter.toLowerCase();

        try {

            if (parameter.contains("help")) {
                help();
                return;
            }


            if (parameter.contains("ftp")) {
                try {
                    hostName = parameter.substring(4, parameter.length());
                    ftp(hostName);
                } catch (Exception ex) {
                    System.out.println("Invalid Command.\nType Help For More Information Or Try Another FTP Server.");
                    getCmd();
                }
            }


            if (parameter.contains("list")) {
                isConnected();
                list();
                return;
            }

            if (parameter.contains("curdir")) {
                isConnected();
                curdir();
                return;
            }


            if (parameter.contains("goto")) {
                isConnected();
                try {
                    String path = parameter.substring(5, parameter.length());
                    cd(path);
                } catch (Exception ex) {

                }
            }

            if (parameter.contains("quit")) {
                isConnected();
                quit();
            }


            if (parameter.substring(0, 3).equals("get")) {
                isConnected();
                try {
                    String file = parameter.substring(4, parameter.length());
                    get(file);
                } catch (Exception ex) {

                }
            }

            if (parameter.substring(0, 4).equals("send")) {
                isConnected();
                try {
                    String file = parameter.substring(5, parameter.length());
                    send(file);
                } catch (Exception ex) {

                }
            }

            if (parameter.substring(0, 4).equals("mget")) {
                isConnected();
                try {
                    String files = parameter.substring(4, parameter.length());
                    mget(files);
                } catch (Exception ex) {

                }
            }

            if (parameter.substring(0, 5).equals("msend")) {
                isConnected();
                try {
                    String files = parameter.substring(5, parameter.length());
                    msend(files);
                } catch (Exception ex) {

                }
            }
        } catch (Exception ex) {

        }
        getCmd();
    }

    private void isConnected() {
        if (client == null) {
            System.out.println("You Must First Connect To Server With This CMD : ftp <host/ip>");
            getCmd();
        }
    }

    private void ftp(String hostName) throws IOException {

        client = new ReliableUDPClient(InetAddress.getByName(hostName), portNumber);
        client.connect();

        sendMessage("ftp " + hostName);
        getCmd();
    }

    private void list() {

        sendMessage("list");
        getCmd();
    }

    private void curdir() {
        sendMessage("curdir");
        getCmd();
    }

    private void cd(String path) {
        sendMessage("cd " + path);
        getCmd();
    }

    private void get(String file) {
        System.out.println("You Want Get 1 File : " + file);
        sendMessage("get " + file);
        getCmd();
    }

    private void send(String file) {
        System.out.println("You Want Send 1 File : " + file);
        sendMessage("send " + file);

        getCmd();
    }

    // hazf
    private void mget(String files) {


        getCmd();
    }

    private void msend(String files) {
        System.out.println("Your Want Send This Files : " + files);


        getCmd();
    }

    private void quit() {
        System.out.println("You Have Been Disconnected From Server.\r\n");


    }

    private void help() {
        System.out.println(
                "Help Menu\n" +
                        "--------------------\n" +
                        "1- FTP <host/ip>\n" +
                        "2- LIST\n" +
                        "3- CURDIR" + "\n" +
                        "4- GOTO <path>\n" +
                        "5- GET <file>\n" +
                        "6- SEND <file>\n" +
                        "7- MGET <file1> <file2> ...\n" +
                        "8- MSEND <file1> <file2> ...\n" +
                        "9- QUIT\n\n");
    }

    private void getCmd() {
        Scanner input = new Scanner(System.in);
        System.out.print("> ");
        String cmd = input.nextLine();
        MainMenu(cmd);
    }

    public void sendMessage(String msg) {


        if (msg.contains("get") || msg.contains("send")) {

            String fileName;
            if (msg.contains("get")) {

                String curDir = System.getProperty("user.dir");
                fileName = msg.substring(4, msg.length());


                try {
                    String msg1 = "FIG:" + fileName + "*@*";
                    System.out.println(msg1);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    outputStream.write(msg1.getBytes());
                    outputStream.flush();
                    try {
                        client.send(outputStream);
                        InputStream receive = client.receive();
                        OutputStream fileOutputStream = null;
                        String path = curDir + "\\Downloads\\" + fileName;
                        System.out.println(path);
                        File file = new File(path);
                        fileOutputStream = new FileOutputStream(file);
                        int read = 0;
                        byte[] bytes = new byte[1024];

                        while ((read = receive.read(bytes)) != -1) {
                            fileOutputStream.write(bytes, 0, read);
                        }

                        System.out.println("File Successfully Created in " + path);


                    } catch (Exception e) {
                        System.out.println("Cannot Send." + e);
                    } finally {

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (msg.contains("send")) {

                String filePath = msg.substring(5, msg.length());

                String msg1 = "FIS:" + filePath + "*@*";
                System.out.println(msg1);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {
                    outputStream.write(msg1.getBytes());
                    outputStream.flush();
                    client.send(outputStream);

                    FileInputStream fis = null;

                    try {
                        File file = new File(filePath);
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

                    client.send(bos);
                    System.out.println("File Send To Server.");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


        } else {

            try {
                String msg1 = "CMD:" + msg + "*@*";
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(msg1.getBytes());
                outputStream.flush();
                try {
                    client.send(outputStream);
                    InputStream receive = client.receive();
                    System.out.println(readString(receive));

                } catch (Exception e) {
                    System.out.println("Cannot Send.");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
