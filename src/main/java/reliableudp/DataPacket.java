package reliableudp;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Mojtaba on 3/26/2015.
 */
public class DataPacket {
    public static final int PACKET_SIZE = 4112;
    public static final int HEADER_SIZE = 16;
    public static final int DATA_SIZE = 4096;
    private ByteBuffer buffer = ByteBuffer.allocate(PACKET_SIZE);


    public DataPacket(long seq, int size, int connection, byte[] data) {
        buffer.putLong(seq);
        buffer.putInt(size);
        buffer.putInt(connection);
        if (data != null) {
            buffer.put(data, 0, data.length);
        }
        buffer.flip();
    }

    public DataPacket(byte[] data, int length) {
        buffer.put(data, 0, length);
        buffer.flip();//need flip
    }

    public DataPacket(byte[] data) {
        buffer.put(data, 0, data.length);
        buffer.flip();//need flip
    }


    /**
     * it is not writable.
     */
    public byte[] getBytes() {
        return buffer.array().clone();
    }


    public long getSeq() {
        return buffer.getLong(0);
    }

    public int getSize() {
        return buffer.getInt(Long.BYTES);
    }

    public byte[] getData() {
        byte[] array = buffer.array();
        return Arrays.copyOfRange(array, Long.BYTES + Integer.BYTES + Integer.BYTES, array.length);
    }

    public int getConnection() {
        return buffer.getInt(Long.BYTES + Integer.BYTES);
    }

    public int getLimit() {
        return buffer.limit();
    }

    @Override
    public String toString() {

        return String.format("reliableudp.Connection: %d\nSEQ: %d\nSize: %d\nDataSize: %d",
                getConnection(), getSeq(), getSize(), getLimit() - 16);
    }
}
