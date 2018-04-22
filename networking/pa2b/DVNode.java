package pa2;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

class DVNode
{
    static int n_nodes;
    static class DV{
        static int node_num;
        static int[] dv;
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

        HashMap<Integer, String> neighborIP = new HashMap<>();
        DV dv;
        dv = null;
        DV.dv = new int[n_nodes];

//        while(true) {

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
             * Set up UDP
             */

            DatagramSocket clientSocket = new DatagramSocket(port);
            ObjectStream stream = new ObjectStream();

            /*
             * Send ip Address
             */
            stream.sendObj(ip, host, port, clientSocket);

            /*
             * Receive datagram
             */
            int node = (int) stream.receiveObj(clientSocket);
            System.out.printf("Node # = %d\n",node);

            /*
             * Receive HashMap<Integer, String> neighborIp()
             */
//            @SuppressWarnings("unchecked")
            Object obj  = stream.receiveObj(clientSocket);
            System.out.println(obj.toString());

            neighborIP = (HashMap<Integer, String>)obj;
            for (Integer i : neighborIP.keySet());
            for (String s : neighborIP.values());
//            System.out.println(neighborIP);

            /*
             * Receive dv Object
             */
            DV.node_num = (int)stream.receiveObj(clientSocket);
            DV.dv = (int[]) stream.receiveObj(clientSocket);
//            System.out.println(dv.node_num + " " + Arrays.toString(dv.dv));


            /*
             * Set up MultiCast
             */
            int multiPort = 11188;
            MulticastSocket multiOutSoc = new MulticastSocket(multiPort);
            MulticastSocket multiInSoc = new MulticastSocket(multiPort);
            String changeIP;
            if(node < 10){
                changeIP = "230.111.0.00" + Integer.toString(node);
//                System.out.println(changeIP);
            } else{
                changeIP = "230.111.0.0" + Integer.toString(node);
//                System.out.println(changeIP);
            }
            InetAddress multiIP = InetAddress.getByName(changeIP);
            multiInSoc.joinGroup(multiIP);
            multiOutSoc.joinGroup(multiIP);
            //now need to form the list of new neighbor IPs
            for(int i = 0; i < n_nodes; i++){
                if(neighborIP.containsKey(i)){
                    String neighbor = neighborIP.get(i);
                    String multiOut, multiIn;
                    System.out.printf("Node#: %d nIP.get(%d) contains: %s\n", node, i, neighbor);
                    if(i < 10){
                        multiOut = "230.111.0.00" + Integer.toString(i);
                        multiIn = "230.111.7.00" + Integer.toString(i);
//                        System.out.println(changeIP);

                    } else{
                        multiOut = "230.111.0.00" + Integer.toString(i);
                        multiIn = "230.111.7.00" + Integer.toString(i);
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
                    oos.writeObject(DV.node_num);
                    oos.flush();
                    oos.writeObject(DV.dv);
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
                    int fromNode = (int)ois.readObject();
                    int[] arrayIn = (int[])ois.readObject();
                    ois.close();
                    System.out.printf("Node #: %d Received %s from node#: %d in Multicast\n", node, Arrays.toString(arrayIn), fromNode);
                }
            }
            multiInSoc.leaveGroup(multiIP);
            multiOutSoc.leaveGroup(multiIP);
            multiInSoc.close();
            multiOutSoc.close();




        } catch (IOException e) {
                System.out.println("Problem with server connecting");
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            System.out.println("Problem with class not found connecting");
            e.printStackTrace();
        }
    }
}
