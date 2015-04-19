package reliableudp;

import java.net.DatagramSocket;

/**
 * Created by Mojtaba on 4/7/2015.
 */
public abstract class ClientTask implements Runnable {

    DatagramSocket socket;

    public ClientTask(DatagramSocket socket) {
        this.socket = socket;
    }
}
