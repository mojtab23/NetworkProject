
import reliableudp.ReliableUDPClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Mojtaba on 4/19/2015.
 */
public class TestClientFile {

    public static void main(String[] args) {


        try {
            ReliableUDPClient client = new ReliableUDPClient(InetAddress.getByName("127.0.0.1"), 10024);
            client.connect();


            File file = new File("E:\\tamrin\\Intellij\\MojtabaFileManager\\UDP\\src\\test\\java\\p.jpg");

            FileInputStream fis = new FileInputStream(file);
            //System.out.println(file.exists() + "!!");
            //InputStream in = resource.openStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            try {
                for (int readNum; (readNum = fis.read(buf)) != -1; ) {
                    bos.write(buf, 0, readNum); //no doubt here is 0
                    //Writes len bytes from the specified byte array starting at offset off to this byte array output stream.
                }
            } catch (IOException ex) {
            }

            client.send(bos);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
