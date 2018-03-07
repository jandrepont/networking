package pa2;
import java.io.*;
import java.net.*;
public class ObjectStream {

    protected static int port;

    public void sendObj(Object object, String receiverIP, int port, DatagramSocket dgram) {

        try {
            /*
             * Set up Steam & oos
             * write object to bytestream via oos
             */
            InetAddress address = InetAddress.getByName(receiverIP);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1000);
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(byteStream));
//            oos.flush();
            oos.writeObject(object);
            oos.flush();

            /*
             * write stream to buffer
             */
            byte[] buffer = byteStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            dgram.send(packet);
            oos.close();
        } catch (UnknownHostException e)
        {
            System.err.printf("Unknown receiverIP in sendObj\n");
            e.printStackTrace();
        } catch (IOException e){
            System.err.printf("IOException in sendObj\n");
            e.printStackTrace();
        }
    }

    public Object receiveObj(DatagramSocket dgram){
        try {
            /*
             * Receive binary packet and convert using ois
             */
            byte[] buffer = new byte[1000];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            dgram.receive(packet);
            port = packet.getPort();

            /*
             * handle with bytestream and make object using ois
             */
            ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer);
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(byteStream));
            Object object = ois.readObject();
            ois.close();
            return(object);

        } catch (IOException e) {
            System.err.printf("IOException in receiveObj: %s\n");
            e.printStackTrace();
            System.exit(0);
        } catch (ClassNotFoundException e){
            System.err.printf("ClassNotFoundException in receiveObj: %s\n");
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }
}