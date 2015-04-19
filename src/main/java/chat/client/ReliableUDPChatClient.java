package chat.client;

import reliableudp.DataPacket;
import reliableudp.ReliableUDPClient;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/**
 * Created by Mojtaba on 4/19/2015.
 */
public class ReliableUDPChatClient extends ReliableUDPClient {

    public ReliableUDPChatClient(InetAddress serverAddress, int serverPort) {
        super(serverAddress, serverPort);
    }

    public boolean connect(String userName) {
        try {
            socket = new DatagramSocket(0);
            DataPacket connectPacket;
            connectPacket = new DataPacket(0, 0, 0, userName.getBytes());
            DatagramPacket request = new DatagramPacket(connectPacket.getBytes(),
                    connectPacket.getLimit(), serverAddress, serverPort);

            socket.setSoTimeout(0);
            while (!connectionIsOpen) {
                socket.send(request);

                byte[] bytes1 = new byte[DataPacket.PACKET_SIZE];
                DatagramPacket p = new DatagramPacket(bytes1, DataPacket.PACKET_SIZE);
                try {
                    socket.receive(p);
                    byte[] bytes = Arrays.copyOf(p.getData(), p.getLength());
                    connectPacket = new DataPacket(bytes);
                    connectionId = connectPacket.getConnection();
                    connectionIsOpen = true;
                } catch (SocketTimeoutException ignored) {
                }
            }
            if (connectionId == -1) return false;
            createSocket();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
