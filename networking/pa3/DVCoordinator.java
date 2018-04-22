package pa3;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.io.Serializable;
public class DVCoordinator extends Thread {

//    DV dv = new DV();
    static int nodes, serverPort;
    static DV[] distVectors;
    static String[][] adjList;
    private static final int inf = Integer.MAX_VALUE;
    static HashMap<Integer, String> nodeIPtable;



    // A utility function to find the vertex with minimum distance value,
    // from the set of vertices not yet included in shortest path tree
    static final int V=nodes;
    static int minDistance(int dist[], Boolean sptSet[])
    {
        // Initialize min value
        int min = Integer.MAX_VALUE, min_index=-1;

        for (int v = 0; v < V; v++)
            if (sptSet[v] == false && dist[v] <= min)
            {
                min = dist[v];
                min_index = v;
            }

        return min_index;
    }

    // A utility function to print the constructed distance array
    static void printSolution(int dist[], int n)
    {
        System.out.println("Vertex   Distance from Source");
        for (int i = 0; i < V; i++)
            System.out.println(i+" tt "+dist[i]);
    }

    // Funtion that implements Dijkstra's single source shortest path
    // algorithm for a graph represented using adjacency matrix
    // representation
    static void dijkstra(int graph[][], int src)
    {
        int dist[] = new int[V]; // The output array. dist[i] will hold
                                 // the shortest distance from src to i

        // sptSet[i] will true if vertex i is included in shortest
        // path tree or shortest distance from src to i is finalized
        Boolean sptSet[] = new Boolean[V];

        // Initialize all distances as INFINITE and stpSet[] as false
        for (int i = 0; i < V; i++)
        {
            dist[i] = Integer.MAX_VALUE;
            sptSet[i] = false;
        }

        // Distance of source vertex from itself is always 0
        dist[src] = 0;

        // Find shortest path for all vertices
        for (int count = 0; count < V-1; count++)
        {
            // Pick the minimum distance vertex from the set of vertices
            // not yet processed. u is always equal to src in first
            // iteration.
            int u = minDistance(dist, sptSet);

            // Mark the picked vertex as processed
            sptSet[u] = true;

            // Update dist value of the adjacent vertices of the
            // picked vertex.
            for (int v = 0; v < V; v++)

                // Update dist[v] only if is not in sptSet, there is an
                // edge from u to v, and total weight of path from src to
                // v through u is smaller than current value of dist[v]
                if (!sptSet[v] && graph[u][v]!=0 &&
                        dist[u] != Integer.MAX_VALUE &&
                        dist[u]+graph[u][v] < dist[v])
                    dist[v] = dist[u] + graph[u][v];
        }

        // print the constructed distance array
        printSolution(dist, V);
    }

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
                System.out.println(Arrays.toString(adjList[i]));
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
        System.out.println(distVectors.length);
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
        distVectors[0].node_num = 10;
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
            System.out.println(Arrays.toString(distVectors[sourceNode].dv));

        }

    }

    static public HashMap<Integer, String> neighborIp(int currentNode){
        HashMap<Integer, String> neighborIPTable = new HashMap<>();
        for(int i = 0; i < nodes; i++){
            if ((distVectors[currentNode].dv[i] < inf) && (distVectors[currentNode].dv[i] > 0)){
                neighborIPTable.put(i, nodeIPtable.get(i));
            }
        }
        return neighborIPTable;
    }


    public static void main(String args[]) {

        int currentNode;
        currentNode = 0;

        try {
            nodes = Integer.parseInt(args[0]);
            serverPort = Integer.parseInt(args[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
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
            DVSender dvs = new DVSender();
            DVReceiver dvr = new DVReceiver();

            while(currentNode < nodes)
            {
                /*
                 * Receive Datagram
                 */
                String nodeIP = (String)dvr.receiveObj(server);

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
                            int port = dvr.port;
                            System.out.println(port);
                            dvs.sendObj(entry.getKey(), nodeIP, port, server);
                        }
                    }
                } else {
                    nodeIPtable.put(currentNode, nodeIP);
                    /*
                     * Get credentials to send datagram to currentNode
                     */
                    int port = dvr.port;
                    System.out.printf("RECEIVED: %s from node: %d w/ port = %d\n", nodeIP, currentNode, port);
                    dvs.sendObj(currentNode, nodeIP, port, server);
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
                // System.out.println(temp.toString());
                dvs.sendObj(temp, nodeIPtable.get(currentNode), serverPort, server);
                dvs.sendObj(distVectors[currentNode], nodeIPtable.get(currentNode), serverPort, server);
                currentNode++;
            }
            System.out.println(nodeIPtable.toString());

            int array[][] = new int[5][];
            for(DV deevee: distVectors){
                array[i] = deevee.dv;
            }
            dijkstra(gp, 0);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Problem with server connecting");
        }

    }
}
