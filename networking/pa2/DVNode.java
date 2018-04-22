package pa2;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.io.Serializable;
import java.lang.Thread;
class DVNode extends Thread {

    static int n_nodes;
    static DV dv = new DV();
    static int node;
    static DVSender dvs = null;//, ostream;
    static DVReceiver dvr = null; //, istream;
    static HashMap<Integer, String> neighborIPTable = new HashMap<>();
    static InetAddress multiIP;


    /*
     * Class vars for multicastSocket
     */
     static int multiPort = 11188;
     static MulticastSocket multiOutSoc;
     static MulticastSocket multiInSoc;




    /*
     * This method sets up the DV object and UDP with all neighbords and IPtable
     */
    public static void init_UDP(String r_host, int r_port){
        // String host = "";
        // int port = 0;
        String host = r_host;
        int port = r_port;

        try {
            /*
             * Get external IP
             */
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = in.readLine();
            in.close();

            /*
             * Send info to DVCoordinator
             */
            DatagramSocket clientSocket = new DatagramSocket(port);

            /*
             * Send ip Address
             */
            dvs = new DVSender();
            dvs.sendObj(ip, host, port, clientSocket);

            /*
             * Receive datagram
             */
            dvr = new DVReceiver();
            node = (int) dvr.receiveObj(clientSocket);
            System.out.printf("Node # = %d\n", node);

            /*
             * Receive HashMap<Integer, String> neighborIp()
             */
            Object obj = dvr.receiveObj(clientSocket);

            neighborIPTable = (HashMap<Integer, String>) obj;
            for (Integer i : neighborIPTable.keySet()) ;
            for (String s : neighborIPTable.values()) ;

            /*
             * Receive dv Object
             */
            dv = (DV) dvr.receiveObj(clientSocket);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Problem with server connecting");
            e.printStackTrace();
        }

        /*
         * Set up MultiCast (UDP)
         *
         */
        try {
            int multiPort = 11188;
            MulticastSocket multiOutSoc = new MulticastSocket(multiPort);
            MulticastSocket multiInSoc = new MulticastSocket(multiPort);
            String changeIP;
            if (node < 10) {
                changeIP = "230.111.0.00" + Integer.toString(node);
//                System.out.println(changeIP);
            } else {
                changeIP = "230.111.0.0" + Integer.toString(node);
//                System.out.println(changeIP);
            }
            multiIP = InetAddress.getByName(changeIP);
            multiInSoc.joinGroup(multiIP);
            multiOutSoc.joinGroup(multiIP);
            //now need to form the list of new neighbor IPs
            for (int i = 0; i < n_nodes; i++) {
                if (neighborIPTable.containsKey(i)) {
                    String neighbor = neighborIPTable.get(i);
                    String multiOut, multiIn;
                    System.out.printf("Node#: %d nIP.get(%d) contains: %s\n", node, i, neighbor);
                    if (i < 10) {
                        multiOut = "230.111.0.00" + Integer.toString(i);
                        multiIn = "230.111.7.00" + Integer.toString(i);
//                        System.out.println(changeIP);

                    } else {
                        multiOut = "230.111.0.0" + Integer.toString(i);
                        multiIn = "230.111.7.0" + Integer.toString(i);
//                      System.out.println(changeIP);

                    }
                    InetAddress groupIn = InetAddress.getByName(multiIn);
                    multiInSoc.joinGroup(groupIn);
                    InetAddress groupOut = InetAddress.getByName(multiOut);
                    multiOutSoc.joinGroup(groupOut);

                   /*
                    * set up buffer for send
                    */
                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1000);
                    ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                    oos.writeObject(dv);
                    oos.flush();


                    /*
                    * write stream to outbuff
                    */
                    byte[] outbuff = byteStream.toByteArray();
                    DatagramPacket outpacket = new DatagramPacket(outbuff, outbuff.length, groupOut, multiPort);
                    multiOutSoc.send(outpacket);
                    oos.close();

                    /*
                     * Receive binary packet and convert using ois
                     */
                    byte[] inbuff = new byte[1000];
                    DatagramPacket inpacket = new DatagramPacket(inbuff, inbuff.length, groupIn, multiPort);
                    multiInSoc.receive(inpacket);

                    /*
                     * handle with bytestream and make object using ois
                     */
                    ByteArrayInputStream inbyteStream = new ByteArrayInputStream(inbuff);
                    ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(inbyteStream));
                    dv = (DV) ois.readObject();
                    ois.close();
                    System.out.printf("Node #: %d Received %s from node#: %d in Multicast\n", node, Arrays.toString(dv.dv), dv.node_num);
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        } catch(ClassNotFoundException e ){
            e.printStackTrace();
        }
    }




    public static void main(String args[]) {
        String host = "";
        int port = 0;
        try {
            host = args[0];
            port = Integer.parseInt(args[1]);
            n_nodes = Integer.parseInt(args[2]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Be sure to run program as java pa2.DVNode <hostIP> <port#> <# of nodes>");
        }
        HashMap<Integer, String> neighborIPTable = new HashMap<>();
        DV dv = new DV();
        dv.dv = new int[n_nodes];
        init_UDP(host, port);
        for (Integer key : neighborIPTable.keySet()) {
            System.out.printf("Key = %d, w/ entry = %s", key, neighborIPTable.get(key));
        }
        // String msg = "Hello";
        // try{
        //     InetAddress group = InetAddress.getByName("230.111.0.0");
        //     DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(),
        //     group, 11188);
        //     multiOutSoc.send(hi);
        //
        //     // get their responses!
        //     byte[] buf = new byte[1000];
        //     DatagramPacket recv = new DatagramPacket(buf, buf.length);
        //     multiInSoc.receive(recv);
        // } catch(IOException e){
        //     e.printStackTrace();
        // }


        // try{
        //     multiInSoc.leaveGroup(multiIP);
        //     multiOutSoc.leaveGroup(multiIP);
        //     multiInSoc.close();
        //     multiOutSoc.close();
        // } catch(IOException e){
        //     System.out.println("Error exiting group UDP");
        // }

    }

}
