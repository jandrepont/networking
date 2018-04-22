package pa2;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedInputStream;


class DVReceiver {


    private static MulticastSocket multiIn;
    protected static int port;
    protected ArrayList<InetAddress> sockets = new ArrayList<>();

    public DVReceiver(){}


    public byte[] receiveFromNeighbor(int packetSize, int index) {

        DatagramPacket inPack = null;
        byte[] buffer = new byte[packetSize];
        byte[] data = new byte[1000];
//        try{

//            multiIn.setSoTimeout(1000);
//        } catch(SocketException e){
//            e.printStackTrace();
//        }
        index = index % sockets.size();
        /*
         * Receive binary packet and convert using ois
         */
        System.out.println(packetSize);
        System.out.println(index);
        System.out.println(sockets.size());

        try {
            System.out.printf("Receiving %d from %s\n", packetSize, sockets.get(index).toString());
            DatagramPacket inpacket = new DatagramPacket(buffer, packetSize);//) sockets.get(index), 11188);
//            socket.receive(inpacket);
            multiIn.receive(inpacket);
            data = inpacket.getData();

        } catch(IOException e){
            e.printStackTrace();
        }


//        multiIn.close();

        return data;
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