package reliableudp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Mojtaba on 3/26/2015.
 */
public class MyInputStream extends InputStream {

    final List<Byte> bytes = new ArrayList<>();
    private volatile boolean hasMore = true;


    @Override
    public int read() throws IOException {

        synchronized (bytes) {

            while (bytes.isEmpty()) {
                if (!hasMore) {
                    return -1;
                }
                try {
                    bytes.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            return bytes.remove(0);

        }
    }


    public void put(boolean hasMore, Byte[] data) {

        synchronized (bytes) {
            bytes.addAll(Arrays.asList(data));
            // Wake up anything blocking on list.wait()
            // Note that we know that only one waiting
            // call can complete (since we only added one
            // item to process. If we wanted to wake them
            // all up, we'd use list.notifyAll()
            this.hasMore = hasMore;
            bytes.notify();
        }
    }

}
