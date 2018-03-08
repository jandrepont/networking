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

    static int nodes;
    static int[][] distVectors;
    static String[][] adjList;
    private static final int inf = Integer.MAX_VALUE;
    static HashMap<Integer, String> nodeIPtable;
    static HashMap<Integer, String> neighborIPtable;




    public static void parseAdjFile(String filename){
        try{
            //initialize adjlist
            String[][] fullList = new String[nodes][nodes];
            adjList = fullList;
            BufferedReader buff = new BufferedReader(new FileReader(filename));
            String myLine = null;
            String[] array;
            int i = 0;
            while ( (myLine = buff.readLine()) != null) {
                myLine = myLine.replaceAll("[\\[]", "");
                myLine = myLine.replaceAll("[^0-9:,]", "");
                array = myLine.split(",");
                adjList[i] = array;
                System.out.println(Arrays.toString(adjList[i]));
                i++;
            }
            if(i != nodes){
                System.out.printf("Warning: nodes != to # of entries in adjacency file\n");
            }

        } catch(FileNotFoundException e){
            System.out.println("Filenot found in parseAdjFile");
            e.printStackTrace();
        } catch(IOException e){
            System.out.println("IOexception in parseAdjFile");
            e.printStackTrace();
        }
    }

    public static void initDistVect(){

        //initialize dvs
        int[][] tempDV = new int[nodes][nodes];
        for(int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                tempDV[i][j] = inf;
            }
        }
        distVectors = tempDV;

        int sourceNode=0, destNode=0, distance=0, substLen=0;
        substLen = 0;
        String vector, subs=null;
        for(sourceNode = 0; sourceNode < nodes; sourceNode++){
            distVectors[sourceNode][sourceNode] = 0;
            for(int j = 0; j < adjList[sourceNode].length; j++){
                vector = adjList[sourceNode][j];
                substLen = vector.indexOf(":");
                destNode = Integer.parseInt(vector.substring(0,substLen));
                distance = Integer.parseInt(vector.substring(substLen+1, vector.length()));
                distVectors[sourceNode][destNode] = distance;
            }
        }
    }

    public static void main(String args[]) {

        int currentNode;
        nodes = Integer.parseInt(args[0]);
        currentNode = 0;
        HashMap<Integer, String> nodeIPtable = new HashMap<>();
        parseAdjFile("pa2/adjacencyList.txt");
        initDistVect();


//        try {
//             /*
//             * Get external IP
//             */
//            URL whatismyip = new URL("http://checkip.amazonaws.com");
//            BufferedReader in = new BufferedReader(new InputStreamReader(
//                    whatismyip.openStream()));
//            String ip = in.readLine();
//            System.out.println(ip);
//            /*
//             * Set up UDP
//             */
//            DatagramSocket server = new DatagramSocket(9876);
//            ObjectStream stream = new ObjectStream();
//
//            while(currentNode < nodes)
//            {
//                /*
//                 * Receive Datagram
//                 */
//                String nodeIP = (String)stream.receiveObj(server);
//                System.out.printf("RECEIVED: %s from node: %d\n", nodeIP, currentNode);
//
//                /*
//                 * store in hashmap and check for redundencies
//                 * public boolean containsValue(Object value)
//                 * Returns true if this map maps one or more keys to the specified value.
//                 */
//                boolean contains = nodeIPtable.containsValue(nodeIP);
//                if (contains) {
//                    System.out.printf("IP: %s already has an assigned node", nodeIP);
//                    for (Object key: nodeIPtable.entrySet()) {
//                        Map.Entry entry = (Map.Entry) key;
//                        if(entry.getValue().equals(nodeIP)){
//                            int port = stream.port;
//                            System.out.println(port);
//                            stream.sendObj(entry.getKey(), nodeIP, port, server);
//                        }
//                    }
//                } else {
//                    nodeIPtable.put(currentNode, nodeIP);
//                    /*
//                     * Get credentials to send datagram to currentNode
//                     */
//                    int port = stream.port;
//                    stream.sendObj(currentNode, nodeIP, port, server);
//                    currentNode++;
//                }
//            }
//                server.close();
//            System.out.println(nodeIPtable.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("Problem with server connecting");
//        }
    }
}
