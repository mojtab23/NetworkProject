

import reliableudp.DataPacket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * Created by mojtab23 on 4/4/15.
 */
public class SimpleUDPClient {

    private final static int PORT = 10024;
    private static final String HOSTNAME = "127.0.0.1";


    public static void main(String[] args) {
        SimpleUDPClient simpleUDPClient = new SimpleUDPClient();
        simpleUDPClient.sendData();
    }


    public void sendData() {

        try (DatagramSocket socket = new DatagramSocket(0)) {
//            socket.setSoTimeout(10000);

            InetAddress host = InetAddress.getByName(HOSTNAME);

//            byte[] bytes = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

            DataPacket connectPacket = new DataPacket(0, 0, 0, null);
            DatagramPacket request = new DatagramPacket(connectPacket.getBytes(), connectPacket.getLimit(), host, PORT);
            socket.send(request);
            byte[] bytes1 = new byte[DataPacket.PACKET_SIZE];
            DatagramPacket p = new DatagramPacket(bytes1, DataPacket.PACKET_SIZE);
            socket.receive(p);
            connectPacket = new DataPacket(Arrays.copyOf(p.getData(), p.getLength()));
            System.out.println("connected.\n" + connectPacket);

            DataPacket dataPacket = new DataPacket(1, 0, connectPacket.getConnection(), "salam".getBytes());
            request = new DatagramPacket(dataPacket.getBytes(), dataPacket.getLimit(), host, PORT);
            socket.send(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
