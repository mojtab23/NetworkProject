package chat;

import reliableudp.Connection;
import reliableudp.DataPacket;
import reliableudp.ReliableUDPServer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Mojtaba on 3/29/2015.
 */
public class ReliableUDPChatServer extends ReliableUDPServer {

    ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    ConnectionHandlerChat connectionHandler;

    public ReliableUDPChatServer(int port) throws SocketException {
        super(port);
    }

    @Override
    protected void createNewConnection(InetAddress address, int port, DataPacket packet) {
        Random random = new Random();
        final int[] connectionId = new int[1];
        Connection connection = new Connection(super.socket, address, port);
        //if connection exist.
        final boolean[] exist = {false};
        String userName = new String(packet.getData(), 0, packet.getLimit() - DataPacket.HEADER_SIZE);
        if (!(exist[0] = users.containsKey(userName))) {
            exist[0] = clients.entrySet().stream().anyMatch(integerConnectionEntry -> {
                connectionId[0] = integerConnectionEntry.getKey();
                return integerConnectionEntry.getValue().equals(connection);
            });
        } else sendConnectionAccept(address, port, -1);

        if (!exist[0]) {
            while ((connectionId[0] = random.nextInt(Integer.MAX_VALUE)) == 0 || clients.containsKey(connectionId[0])) ;
            connection.setConnectionId(connectionId[0]);
            clients.put(connectionId[0], connection);
            users.put(userName, new User(connection));
            connectionHandler.handleConnection(connection, users, userName);
        }

        sendConnectionAccept(address, port, connectionId[0]);

    }

    @Override
    protected void sendConnectionAccept(InetAddress address, int port, int connectionId) {
        try {
            DataPacket dataPacket;
            if (connectionId == -1) {
                dataPacket = new DataPacket(0, 0, -1, null);
            } else
                dataPacket = new DataPacket(0, 0, connectionId, null);
            DatagramPacket packet = new DatagramPacket(dataPacket.getBytes(), dataPacket.getLimit(), address, port);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setConnectionHandler(ConnectionHandlerChat connectionHandler) {
        this.connectionHandler = connectionHandler;
    }


}