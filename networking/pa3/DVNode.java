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
    static InetAddress myIp;
    static int node;
    static DVSender dvs = null;//, ostream;
    static DVReceiver dvr = null; //, istream;
    static HashMap<Integer, String> neighborIPTable = new HashMap<>();
    static DatagramSocket clientSocket;

    /*
     * Class vars for multicastSocket
     */
    static InetAddress multiIPout, multiIPin;
    static int multiPort;
    static MulticastSocket multiOutSoc = null;
    static MulticastSocket multiInSoc = null;

    /**
     * Class vars for Forwarding
     */
    static FWNode fwnode;



    /*
     * This method sets up the DV object and UDP with all neighbords and IPtable
     */
    public static void init_DV_data(String r_host, int r_port){
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
            System.out.printf("My ip = %s\n", ip);
            in.close();
            myIp = InetAddress.getByName(ip);


            /*
             * Send info to DVCoordinator
             */
            clientSocket = new DatagramSocket(port);

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
            System.out.printf("Object = %s\n",obj.toString());
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
            clientSocket.close();
            Thread.sleep(5000);
            fwnode = new FWNode(12345, neighborIPTable.size(),ftable, myIp, node);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Problem with server connecting");
            e.printStackTrace();
        } catch(InterruptedException e){
            System.out.println("ughhh");
        }
    }

    public static void init_UDP(String r_host, int port){
        /*
         * Set up MultiCast (UDP)
         *
         */
        try {
            multiPort = port;
            multiOutSoc = new MulticastSocket(multiPort);
            multiInSoc = new MulticastSocket(multiPort);
            String multiOut, multiIn;
            if (node < 10) {
                multiOut = "230.111.0.00" + Integer.toString(node);
                multiIn = "230.111.7.00" + Integer.toString(node);
//                System.out.println(changeIP);
            } else {
                multiOut = "230.111.0.0" + Integer.toString(node);
                multiIn = "230.111.7.00" + Integer.toString(node);
//                System.out.println(changeIP);
            }

            //set up class variable groupIPs
            multiIPout = InetAddress.getByName(multiOut);
            multiIPin = InetAddress.getByName(multiIn);

            //Out connects to itself?
            // multiOutSoc.joinGroup(multiIPout);

            for (Integer key : neighborIPTable.keySet()) {
                // System.out.printf("Key = %d\n", key);
                String neighbor = neighborIPTable.get(key);
                // String multiOut, multiIn;
                if (key < 10) {
                    multiOut = "230.111.0.00" + Integer.toString(key);
                    // multiIn = "230.111.7.00" + Integer.toString(key);
                } else {
                    multiOut = "230.111.0.0" + Integer.toString(key);
                    // multiIn = "230.111.7.0" + Integer.toString(key);
                }

                InetAddress output = InetAddress.getByName(multiOut);
                multiInSoc.joinGroup(output);
                // InetAddress groupOut = InetAddress.getByName(multiIP);
                // multiOutSoc.joinGroup(groupOut);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        /**
         * Now try to send out datagrams of DV to all neighbors
         */
        try{
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
            DatagramPacket outpacket = new DatagramPacket(outbuff, outbuff.length, multiIPout, multiPort);
            multiOutSoc.send(outpacket);
            oos.close();



            for (Integer key : neighborIPTable.keySet()) {
                /*
                 * Receive binary packet and convert using ois
                 */
                byte[] inbuff = new byte[1000];
                DatagramPacket inpacket = new DatagramPacket(inbuff, inbuff.length, multiIPin, multiPort);
                multiInSoc.receive(inpacket);

                /*
                 * handle with bytestream and make object using ois
                 */
                ByteArrayInputStream inbyteStream = new ByteArrayInputStream(inbuff);
                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(inbyteStream));
                dv = (DV) ois.readObject();
                System.out.printf("Node #: %d Received %s from node#: %d in Multicast\n", node, Arrays.toString(dv.dv), dv.node_num);
                ois.close();
            }
        } catch(IOException e){
            e.printStackTrace();
        } catch(ClassNotFoundException e ){
            e.printStackTrace();
        }
    }

    public static void ports_init(){
        HashMap<Integer, PortUser> p_usr = new HashMap<>();
        // PortUser[] p_usr = new PortUser[neighborIPTable.size()];
        int ind = 0;
        for (Integer key : neighborIPTable.keySet()){
            System.out.printf("Node = %d, IP = %s, muliPort = %d \n", key, neighborIPTable.get(key), multiPort);
            p_usr.put(key, new PortUser(node, neighborIPTable.get(key), multiPort));
        }
        fwnode.set_p_usrs(p_usr);
        // System.out.printf("fwnode.p_usr[1].nodeId = %d", fwnode.p_usr[1].nodeId);
        // fwnode.p_usr

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

        /**
         * initializes data structs DV and neighborIPTable
         *
         */
        init_DV_data(host, port);

        /**
         * creates udp connections from neighborIPTable
         */
        multiPort = 11188;
        init_UDP(host, multiPort);
        String msg = "Hello";

        /**
         * Let us suppose node 0 wants to send to node 6 through TCP
         * Need to foward the package for path 0->3->2->5->6
         * This will be done using fwnode and the fw_table
         */

        /**
         * Now have FWNode with receiver and ports. need to initialize
         */
        int index = 0;
        HashMap<Integer, PortUser> p_usr = new HashMap<>();
        for (Integer key : neighborIPTable.keySet()){
            try{
                Thread.sleep(2000);
                // System.out.printf("Node = %d, IP = %s, muliPort = %d \n", key, neighborIPTable.get(key), multiPort);
                System.out.printf("NeighborIP table = %s\n", neighborIPTable.toString());
                p_usr.put(key, new PortUser(key, neighborIPTable.get(key), 12345));
            } catch(InterruptedException e){
                System.out.println("ughhh");
            }
        }
        fwnode.set_p_usrs(p_usr);
        //Now everyone has port users but they are not connected

        for(int key = 0; key < n_nodes; key++){
            if(node == key){
                fwnode.dp_init();
            }
            if(neighborIPTable.containsKey(key)){
                fwnode.p_usr_init(key);
            }
        }

        try{
            Thread.sleep(5000);

        } catch(InterruptedException e){
            e.printStackTrace();
        }
        // String hey = "Hello from 3";
        // fwnode.printConnections();
        if(node == 3){
            try{
                Thread.sleep(20000);
                byte[] pack = new byte[1024];
        		Arrays.fill(pack, (byte)10);
                System.out.print("\nSENDING MESSAGE\n");
                MessageType message = new MessageType(3, 4, pack);
                fwnode.sendNeighbor(message);
                Thread.sleep(10000);
                pack = new byte[1024];
                Arrays.fill(pack, (byte)10);
                System.out.print("\nSENDING MESSAGE\n");
                message = new MessageType(3, 6, pack);
                fwnode.sendNeighbor(message);
                Thread.sleep(10000);
                pack = new byte[1024];
                Arrays.fill(pack, (byte)10);
                message = new MessageType(3, 0, pack);
                System.out.print("\nSENDING MESSAGE\n");
                fwnode.sendNeighbor(message);
            } catch(InterruptedException e){
                System.out.print("Error in sleep from DVNode at sendNeighbor\n");
            }

        }

            while(true){
                MessageType message;
                message = fwnode.receive();
                System.out.printf("Node %d received message from node %d\n", node, message.getSourceNode());
                fwnode.sendNeighbor(message); 
            }


        //     try{
        //         Thread.sleep(10000);
        //         System.out.print("\nSENDING MESSAGE\n");
        //
        //         // for (Integer node_id : neighborIPTable.keySet()){
        //         MessageType message = new MessageType(0, 3, hey.getBytes());
        //         fwnode.sendNeighbor(message);
        //     } catch(InterruptedException e){
        //         System.out.print("Error in sleep from DVNode at sendNeighbor\n");
        //     }
        //
        //     }
        // }
        // if(null!=neighborIPTable.get(0)){
        //     // fwnode.receive();
        // }
    }
}
