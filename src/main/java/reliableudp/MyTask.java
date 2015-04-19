package reliableudp;

/**
 * Created by Mojtaba on 4/7/2015.
 */
public abstract class MyTask implements Runnable {

    private Connection connection;

    public MyTask(Connection connection) {
        this.connection = connection;
    }

}

