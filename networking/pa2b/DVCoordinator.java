package pa2;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
public class DVCoordinator extends Thread {

    static class DV {
        int node_num;
        int[] dv;
    }
    static int nodes, serverPort;
    static DV[] distVectors;
    static String[][] adjList;
    private static final int inf = Integer.MAX_VALUE;
    static HashMap<Integer, String> nodeIPtable;





    public static void parseAdjFile(String filename){
        try{
            //initialize adjlist
            String[][] fullList = new String[10][10];
            adjList = fullList;
            BufferedReader buff = new BufferedReader(new FileReader(filename));
            String myLine = null;
            String[] array;
            int i = 0;
            while ( (myLine = buff.readLine()) != null ) {
                myLine = myLine.replaceAll("[\\[]", "");
                myLine = myLine.replaceAll("[^0-9:,]", "");
                array = myLine.split(",");
                adjList[i] = array;
//                System.out.println(Arrays.toString(adjList[i]));
                i++;
            }
            if(i != nodes){
                System.out.printf("Warning: nodes(%d) != to # of entries in adjacency file(%d)\n",nodes, i);
            }

        } catch(FileNotFoundException e){
            System.out.println("Filenot found in parseAdjFile");
            e.printStackTrace();
        } catch(IOException e){
            System.out.println("IOexception in parseAdjFile");
            e.printStackTrace();
        }
    }

    public static void initDistVect() {

        distVectors = new DV[nodes];
//        DV dv = new DV();
        int sourceNode = 0, destNode = 0, distance = 0, substLen = 0;
        substLen = 0;
        String vector, subs = null;
        //fill in DistVectors w/ DV
        for(int i = 0; i < nodes; i++){
            distVectors[i] = new DV();
            distVectors[i].dv = new int[nodes];
            for(int j = 0; j < nodes; j++){
                distVectors[i].dv[j] = inf;
            }
        }

        for (sourceNode = 0; sourceNode < nodes; sourceNode++) {

            distVectors[sourceNode].node_num = sourceNode;
            distVectors[sourceNode].dv[sourceNode] = 0;

            for (int j = 0; j < adjList[sourceNode].length; j++) {
                vector = adjList[sourceNode][j];
                substLen = vector.indexOf(":");
                destNode = Integer.parseInt(vector.substring(0, substLen));
                distance = Integer.parseInt(vector.substring(substLen + 1, vector.length()));
                if(destNode < nodes){
                    distVectors[sourceNode].dv[destNode] = distance;
                }
            }
            System.out.println(distVectors[sourceNode].node_num + " " + Arrays.toString(distVectors[sourceNode].dv));

        }
    }

    static public HashMap<Integer, String> neighborIp(int currentNode){
        HashMap<Integer, String> neighborIPTable = new HashMap<>();
        for(int i = 0; i < nodes; i++){
            if ((distVectors[currentNode].dv[i] < inf) && (distVectors[currentNode].dv[i] > 0)){
                neighborIPTable.put(i, nodeIPtable.get(i));
            }
        }
//        System.out.println()
        return neighborIPTable;
    }



    public static void main(String args[]) {

        int currentNode;
//        nodes = Integer.parseInt(args[0]);
//        serverPort = Integer.parseInt(args[1]);
        currentNode = 0;

        try{
            nodes = Integer.parseInt(args[0]);
            serverPort = Integer.parseInt(args[1]);
        } catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            System.out.println("Caused by not including arguments.\nEnsure program is run as java pa2.DVCoordinator <# of nodes> <server port#>");
        }

        nodeIPtable = new HashMap<>();
        parseAdjFile("pa2/adjacencyList.txt");
        try {
             /*
             * Get external IP
             */
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = in.readLine();
            System.out.println(ip);
            /*
             * Set up UDP
             */

            DatagramSocket server = new DatagramSocket(null);
            InetSocketAddress address = new InetSocketAddress(ip, serverPort);
            server.bind(address);
            ObjectStream stream = new ObjectStream();

            while(currentNode < nodes)
            {
                /*
                 * Receive Datagram
                 */
                String nodeIP = (String)stream.receiveObj(server);

                /*
                 * store in hashmap and check for redundencies
                 * public boolean containsValue(Object value)
                 * Returns true if this map maps one or more keys to the specified value.
                 */
                boolean contains = nodeIPtable.containsValue(nodeIP);
                if (contains) {
                    System.out.printf("IP: %s already has an assigned node", nodeIP);
                    for (Object key: nodeIPtable.entrySet()) {
                        Map.Entry entry = (Map.Entry) key;
                        if(entry.getValue().equals(nodeIP)){
                            int port = stream.port;
                            System.out.println(port);
                            stream.sendObj(entry.getKey(), nodeIP, port, server);
                        }
                    }
                } else {
                    nodeIPtable.put(currentNode, nodeIP);
                    /*
                     * Get credentials to send datagram to currentNode
                     */
                    int port = stream.port;
                    System.out.printf("RECEIVED: %s from node: %d w/ port = %d\n", nodeIP, currentNode, port);
                    stream.sendObj(currentNode, nodeIP, port, server);
                    currentNode++;
                }
            }


            /*
             * Init distVector, neighborIPtable and send corresponding dv to each DVNode
             */
            currentNode=0;
            initDistVect();
            HashMap<Integer, String> temp = new HashMap<>();
            while(currentNode < nodes){
                temp = neighborIp(currentNode);
                System.out.println(temp.toString());
                stream.sendObj(temp, nodeIPtable.get(currentNode), serverPort, server);
//                Thread.sleep((10000));
                stream.sendObj(distVectors[currentNode].node_num, nodeIPtable.get(currentNode), serverPort, server);
                stream.sendObj(distVectors[currentNode].dv, nodeIPtable.get(currentNode), serverPort, server);
                currentNode++;
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Problem with server connecting");
        }

    }
}
