package reliableudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Mojtaba on 3/29/2015.
 */
public class ReliableUDPServer {

    private int port;
    private Map<Integer, Connection> clients = new ConcurrentHashMap<>();
    private ConnectionHandler connectionHandler;
    private DatagramSocket socket;

    public ReliableUDPServer(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public void accept() {
        Thread thread = new Thread(() -> {


            while (true) {
                try {
                    DatagramPacket request = receive(socket);
                    DataPacket packet = new DataPacket(request.getData(), request.getLength());
                    //when received create new session whit new Thread.
                    System.out.println("new request...");
                    if (packet.getConnection() == 0)
                        createNewConnection(request.getAddress(), request.getPort());
                    else sendToConnection(packet);

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });
        thread.start();
    }

    private void sendToConnection(DataPacket packet) {

        int connectionId = packet.getConnection();
        Connection connection = clients.get(connectionId);
        if (connection != null) {
            connection.put(packet);
        }

    }


    private DatagramPacket receive(DatagramSocket socket) throws IOException {
        DatagramPacket request = new DatagramPacket(
                new byte[DataPacket.PACKET_SIZE], DataPacket.PACKET_SIZE);
        socket.receive(request);
        return request;
    }

    private void createNewConnection(InetAddress address, int port) {
        Random random = new Random();
        final int[] connectionId = new int[1];
        Connection connection = new Connection(socket, address, port);
        //if connection exist.
        final boolean[] exist = {false};
        exist[0] = clients.entrySet().stream().anyMatch(integerConnectionEntry -> {
            connectionId[0] = integerConnectionEntry.getKey();
            return integerConnectionEntry.getValue().equals(connection);
        });


        if (!exist[0]) {
            while ((connectionId[0] = random.nextInt(Integer.MAX_VALUE)) == 0 || clients.containsKey(connectionId[0])) ;
            connection.setConnectionId(connectionId[0]);
            clients.put(connectionId[0], connection);
            connectionHandler.handleConnection(connection);
        }

        sendConnectionAccept(address, port, connectionId[0]);

    }

    private void sendConnectionAccept(InetAddress address, int port, int connectionId) {

        try {
            DataPacket dataPacket = new DataPacket(0, 0, connectionId, null);
            DatagramPacket packet = new DatagramPacket(dataPacket.getBytes(), dataPacket.getLimit(), address, port);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}