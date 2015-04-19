package reliableudp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mojtaba on 4/7/2015.
 */
public class ReliableUDPClient {


    private BlockingQueue<DataPacket> responsePackets = new ArrayBlockingQueue<>(10);
    private BlockingQueue<DataPacket> ackPackets = new ArrayBlockingQueue<>(10);
    private InetAddress serverAddress;
    private int serverPort;
    private DatagramSocket socket;
    private volatile boolean connectionIsOpen = false;
    private int connectionId = 0;
    private long seq = 0;

    public ReliableUDPClient(InetAddress serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

    }

    public void connect() {
        try {
            socket = new DatagramSocket(0);
            DataPacket connectPacket;
            connectPacket = new DataPacket(0, 0, 0, null);
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
            createSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createSocket() {
        Thread thread = new Thread(new ClientTask(socket) {
            @Override
            public void run() {
                while (connectionIsOpen) {
                    try {
                        byte[] bytes = new byte[DataPacket.PACKET_SIZE];
                        DatagramPacket p = new DatagramPacket(bytes, DataPacket.PACKET_SIZE);
                        socket.receive(p);
                        System.out.println("received");
                        byte[] bytes1 = Arrays.copyOf(p.getData(), p.getLength());
                        DataPacket responsePacket = new DataPacket(bytes1);

                        if (responsePacket.getSize() == -1) ackPackets.put(responsePacket);
                        else responsePackets.put(responsePacket);

                    } catch (IOException | InterruptedException e) {
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void send(ByteArrayOutputStream os) throws Exception {
        byte[] bytes = os.toByteArray();
        int dataSize = DataPacket.DATA_SIZE;  // chunk size
        int len = bytes.length;
        int size = len / dataSize;

        seq = 0;
        for (int i = 0; i < len - dataSize + 1; i += dataSize)
            sendReliable(Arrays.copyOfRange(bytes, i, i + dataSize), size--);

        if (len % dataSize != 0)
            sendReliable(Arrays.copyOfRange(bytes, len - len % dataSize, len), size--);

    }

    private void sendReliable(byte[] out, int chunkSize) throws Exception {

        DataPacket dataPacket = new DataPacket(seq, chunkSize,
                connectionId, out);
        DatagramPacket request = new DatagramPacket(dataPacket.getBytes(),
                dataPacket.getLimit(), serverAddress, serverPort);
        DataPacket poll;
        do {
            socket.send(request);
            poll = ackPackets.poll(15, TimeUnit.MINUTES);
        } while (poll.getSeq() != seq);
        seq++;
    }


    public InputStream receive() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        boolean hasNext = true;
        long packetSeq = 0;
        while (hasNext) {
            try {
                DataPacket packet = responsePackets.take();
                if (packetSeq == packet.getSeq()) {
                    stream.write(packet.getData(), 0, packet.getLimit() - DataPacket.HEADER_SIZE);
                    if (packet.getSize() == 0) hasNext = false;
                    sendAck(packetSeq);
                    packetSeq++;
                } else if (packet.getSeq() == packetSeq - 1) {
                    sendAck(packetSeq - 1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        stream.flush();
        return new ByteArrayInputStream(stream.toByteArray());
    }

    private void sendAck(long packetSeq) {
        DataPacket connectPacket;
        try {
            connectPacket = new DataPacket(packetSeq, -1, connectionId, null);
            DatagramPacket ack = new DatagramPacket(connectPacket.getBytes(),
                    connectPacket.getLimit(), serverAddress, serverPort);
            socket.send(ack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
