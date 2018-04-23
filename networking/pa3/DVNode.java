package pa3;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.io.Serializable;
import java.lang.Thread;
class DVNode extends Thread {

    /**
     * Class vars for DVNode
     */
    static int n_nodes;
    static DV dv = new DV();
    static int node;
    static DVSender dvs = null;//, ostream;
    static DVReceiver dvr = null; //, istream;
    static HashMap<Integer, String> neighborIPTable = new HashMap<>();

    /*
     * Class vars for multicastSocket
     */
    static InetAddress multiIP;
    static int multiPort = 11188;
    static MulticastSocket multiOutSoc = null;
    static MulticastSocket multiInSoc = null;

    /**
     * Class vars for Forwarding
     */
    static FWNode fwnode;



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
            Object obj = (Object) dvr.receiveObj(clientSocket);
            System.out.printf("Object = %s",obj.toString());
            neighborIPTable = (HashMap<Integer, String>) obj;
            for (Integer i : neighborIPTable.keySet()) ;
            for (String s : neighborIPTable.values()) ;
            // System.out.println(neighborIPTable.toString());

            /*
             * Receive dv Object
             */
            dv = (DV) dvr.receiveObj(clientSocket);

            /*
             * Receive Forwarding table and initialize fwnode
             */

            obj = (Object) dvr.receiveObj(clientSocket);
            HashMap<Integer,String> ftable = (HashMap<Integer, String>) obj;
            for (Integer i : ftable.keySet());
            for (String s : ftable.values());
            fwnode = new FWNode(11111, neighborIPTable.size(),ftable);
            clientSocket.close();

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
            multiOutSoc = new MulticastSocket(multiPort);
            multiInSoc = new MulticastSocket(multiPort);
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
            for (Integer key : neighborIPTable.keySet()) {
                // System.out.printf("Key = %d\n", key);
                String neighbor = neighborIPTable.get(key);
                String multiOut, multiIn;
                if (key < 10) {
                    multiOut = "230.111.0.00" + Integer.toString(key);
                    multiIn = "230.111.7.00" + Integer.toString(key);
                } else {
                    multiOut = "230.111.0.0" + Integer.toString(key);
                    multiIn = "230.111.7.0" + Integer.toString(key);
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
                // System.out.printf("Node #: %d Received %s from node#: %d in Multicast\n", node, Arrays.toString(dv.dv), dv.node_num);
            }
        } catch(IOException e){
            e.printStackTrace();
        } catch(ClassNotFoundException e ){
            e.printStackTrace();
        }
    }

    public static void ports_init(){

        PortUser[] p_usr = new PortUser[neighborIPTable.size()];
        int ind = 0;
        for (Integer key : neighborIPTable.keySet()){
            p_usr[ind] = new PortUser(key, neighborIPTable.get(key), 11111);
            ++ind;
        }
        System.out.println(Arrays.toString(p_usr));
        fwnode.set_p_usrs(p_usr);

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
        // HashMap<Integer, String> neighborIPTable = new HashMap<>();
        DV dv = new DV();
        dv.dv = new int[n_nodes];
        init_UDP(host, port);
        String msg = "Hello";

        /**
         * Test and see if the multisockets are blocking?
         */
        try{
            multiInSoc.leaveGroup(multiIP);
            multiOutSoc.leaveGroup(multiIP);
            multiInSoc.close();
            multiOutSoc.close();
        } catch(IOException e){
            System.out.println("Error exiting group UDP");
        }

        /**
         * Let us suppose node 0 wants to send to node 6 through TCP
         * Need to foward the package for path 0->3->2->5->6
         * This will be done using fwnode and the fw_table
         */
        // FWN_init();
        /**
         * Now have FWNode with receiver and ports. need to initialize
         */
        ports_init();
        int index = 0;
        for(int listen = 0; listen < n_nodes; listen+=index){
            int attempts = 0;
            if(listen == 0){
                if(node == 0){
                    fwnode.dp_init();
                }
                else if(node == 1 || node == 3 || node == 8){
                    try{
                        Thread.sleep(2000);
                        fwnode.p_usr_init();
                    } catch(InterruptedException e){
                        System.out.println("ughhh");
                    }
                }
            }
            // if(listen == 1){
            //     if(node == 1){
            //         fwnode.dp_init();
            //     }
            //     else if(node == 7){
            //         try{
            //             Thread.sleep(2000);
            //             fwnode.p_usr_init();
            //         } catch(InterruptedException e){
            //             System.out.println("ughhh");
            //         }
            //     }
            // }

            // if(listen == 2){
            //     if(node == 2){
            //         fwnode.dp_init();
            //     }
            //     else if(node == 3 || node == 5 || node == 7){
            //         try{
            //             Thread.sleep(2000);
            //             fwnode.p_usr_init();
            //         } catch(InterruptedException e){
            //             System.out.println("ughhh");
            //         }
            //     }
            // }
            // if(listen == 3){
            //     if(node == 3){
            //         fwnode.dp_init();
            //     }
            //     else if(node == 0 || node == 2 || node == 4){
            //         try{
            //             Thread.sleep(2000);
            //             fwnode.p_usr_init();
            //         } catch(InterruptedException e){
            //             System.out.println("ughhh");
            //         }
            //     }
            // }
            // if(listen == 4){
            //     if(node == 4){
            //         fwnode.dp_init();
            //     }
            //     else if(node == 3 || node == 8){
            //         try{
            //             Thread.sleep(2000);
            //             fwnode.p_usr_init();
            //         } catch(InterruptedException e){
            //             System.out.println("ughhh");
            //         }
            //     }
            // }
            // if(listen == 5){
            //     if(node == 5){
            //         fwnode.dp_init();
            //     }
            //     else if(node == 6){
            //         try{
            //             Thread.sleep(2000);
            //             fwnode.p_usr_init();
            //         } catch(InterruptedException e){
            //             System.out.println("ughhh");
            //         }
            //     }
            // }
            // if(listen == 7){
            //     if(node == 7){
            //         fwnode.dp_init();
            //     }
            //     else if(node == 1 || node == 2){
            //         try{
            //             Thread.sleep(2000);
            //             fwnode.p_usr_init();
            //         } catch(InterruptedException e){
            //             System.out.println("ughhh");
            //         }
            //     }
            // }
            // if(listen == 8){
            //     if(node == 8){
            //         fwnode.dp_init();
            //     }
            //     else if(node == 0 || node == 4){
            //         try{
            //             Thread.sleep(2000);
            //             fwnode.p_usr_init();
            //         } catch(InterruptedException e){
            //             System.out.println("ughhh");
            //         }
            //     }

        }
        // for (Integer key : neighborIPTable.keySet()) {
        //     System.out.printf("Key = %d, w/ entry = %s\n", key, neighborIPTable.get(key));
        //     try{
        //
        //         /*
        //          * set up buffer for send
        //          */
        //         InetAddress group = InetAddress.getByName("230.111.0.00" + Integer.toString(key));
        //         ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1000);
        //         ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(byteStream));
        //         oos.writeObject(msg);
        //         oos.flush();
        //
        //
        //         /*
        //          * write outbuff to stream
        //          */
        //         byte[] outbuff = byteStream.toByteArray();
        //         DatagramPacket hi = new DatagramPacket(outbuff, outbuff.length, group, 11188);
        //         multiOutSoc.send(hi);
        //         oos.close();
        //
        //         // get their responses!
        //         byte[] buf = new byte[1000];
        //         DatagramPacket recv = new DatagramPacket(buf, buf.length);
        //         multiInSoc.receive(recv);
        //
        //         ByteArrayInputStream inbyteStream = new ByteArrayInputStream(buf);
        //         ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(inbyteStream));
        //         Object pls = (Object) ois.readObject();
        //         ois.close();
        //         System.out.println(pls);
        //
        //     } catch(IOException e){
        //         e.printStackTrace();
        //     } catch(ClassNotFoundException e){
        //         e.printStackTrace();
        //     }


        // try{
        //     multiInSoc.leaveGroup(multiIP);
        //     multiOutSoc.leaveGroup(multiIP);
        //     multiInSoc.close();
        //     multiOutSoc.close();
        // } catch(IOException e){
        //     System.out.println("Error exiting group UDP");
        // }

        // }

    }
}
