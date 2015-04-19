

import reliableudp.ReliableUDPClient;

import java.io.*;
import java.net.InetAddress;

/**
 * Created by Mojtaba on 4/19/2015.
 */
public class TestClient {


    public static void main(String[] args) {


        try {
            ReliableUDPClient client = new ReliableUDPClient(InetAddress.getByName("127.0.0.1"), 10024);
            client.connect();
            String s = "salam";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(s.getBytes());
            outputStream.flush();
            client.send(outputStream);
            InputStream receive = client.receive();

            System.out.println(readString(receive));


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String readString(InputStream receive) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        int linNO = 0;
        String line;
        try {

            br = new BufferedReader(new InputStreamReader(receive));
            while ((line = br.readLine()) != null) {
                sb.append(line);
                linNO++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }


}
