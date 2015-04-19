package reliableudp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mojtaba on 3/29/2015.
 */
public class Connection {

    private InetAddress address;
    private int port;
    //    private MyInputStream buffer;
    private long lastSeq = 0;
    private int connectionId = 0;
    private DatagramSocket socket;
    private BlockingQueue<DataPacket> responsePackets = new ArrayBlockingQueue<>(10);
    private BlockingQueue<DataPacket> ackPackets = new ArrayBlockingQueue<>(10);

    public Connection(DatagramSocket socket, InetAddress address, int port) {
        this.socket = socket;
        this.address = address;
        this.port = port;
//        buffer = new MyInputStream();

        // run service in new thread.

//        serviceThread = new Thread(doService());

    }

    public static Byte[] toBytesObject(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        Arrays.setAll(bytes, n -> bytesPrim[n]);
        return bytes;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

//    private Runnable doService() {
//        return () -> {
//
//
//            boolean isHeader = true;
//            int nextByte = 0;
//            try {
//                nextByte = buffer.read();
//                BufferedReader in = new BufferedReader(new InputStreamReader(buffer));
//                String s = in.readLine();
//                System.out.println(s);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        };
//
//    }

    void put(DataPacket packet) {
        try {
            if (packet.getSize() == -1) ackPackets.put(packet);
            else responsePackets.put(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


//    public InputStream getInputStream() {
//        return buffer;
//    }

    @Override
    public boolean equals(Object obj) {
        Connection obj1 = (Connection) obj;
        InetAddress address = obj1.getAddress();
        int port = obj1.getPort();
        return address.equals(this.getAddress()) && port == this.getPort();
    }

    public long getLastSeq() {
        return lastSeq;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public void send(ByteArrayOutputStream os) throws Exception {
        byte[] bytes = os.toByteArray();
        int dataSize = DataPacket.DATA_SIZE;  // chunk size
        int len = bytes.length;
        int size = len / dataSize;


        for (int i = 0; i < len - dataSize + 1; i += dataSize)
            sendReliable(Arrays.copyOfRange(bytes, i, i + dataSize), size);

        if (len % dataSize != 0)
            sendReliable(Arrays.copyOfRange(bytes, len - len % dataSize, len), size);

    }

    private void sendReliable(byte[] out, int chunkSize) throws Exception {
        long seq = 0;
        DataPacket dataPacket = new DataPacket(seq, chunkSize,
                connectionId, out);
        DatagramPacket request = new DatagramPacket(dataPacket.getBytes(),
                dataPacket.getLimit(), address, port);
        DataPacket poll;
        do {
            socket.send(request);
            poll = ackPackets.poll(15, TimeUnit.MILLISECONDS);
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
                    connectPacket.getLimit(), address, port);
            socket.send(ack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

