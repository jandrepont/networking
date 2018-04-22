package pa2;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;



class DVSender {


    static MulticastSocket multiOutSocket;
    protected ArrayList<InetAddress> sockets = new ArrayList<>();

    public DVSender(){}

    public void send2Neighbor(byte[] payload, int index) {


        DatagramPacket outpacket = new DatagramPacket(payload, payload.length, sockets.get(index), 11188);
        try{
            multiOutSocket.send(outpacket);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void sendObj(Object object, String receiverIP, int port, DatagramSocket dgram) {

        try {
            /*
             * Set up Steam & oos
             * write object to bytestream via oos
             */
            InetAddress address = InetAddress.getByName(receiverIP);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1000);
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(byteStream));
            oos.flush();
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


}