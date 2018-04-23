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
import java.util.List;
import java.util.ArrayList;
public class ftable {
    static int nodes, serverPort;
    static DV[] distVectors;
    static String[][] adjList;
    private static final int inf = Integer.MAX_VALUE;
    static HashMap<Integer, String> nodeIPtable;
    static HashMap<Integer, String> best_path = new HashMap<>();
    static List<Map<Integer, String>> listOfMaps = new ArrayList<Map<Integer, String>>();
    static final int V=9;
    private static final int NO_PARENT = -1;



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
                // System.out.println(Arrays.toString(adjList[i]));
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
        int sourceNode = 0, destNode = 0, distance = 0, substLen = 0;
        substLen = 0;
        String vector, subs = null;
        //fill in DistVectors w/ DV
        for(int i = 0; i < nodes; i++){
            distVectors[i] = new DV();
            distVectors[i].dv = new int[nodes];
            for(int j = 0; j < nodes; j++){
                distVectors[i].dv[j] = inf/2;
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
            // System.out.println(Arrays.toString(distVectors[sourceNode].dv));

        }

    }

    // Shortest path Dijkstra
    private static void dijkstra(int[][] adjacencyMatrix, int startVertex)
    {
        int nVertices = adjacencyMatrix[0].length;

        // shortestDistances[i] will hold the
        // shortest distance from src to i
        int[] shortestDistances = new int[nVertices];

        // added[i] will true if vertex i is
        // included / in shortest path tree
        // or shortest distance from src to
        // i is finalized
        boolean[] added = new boolean[nVertices];

        // Initialize all distances as
        // INFINITE and added[] as false
        for (int vertexIndex = 0; vertexIndex < nVertices;
                                            vertexIndex++)
        {
            shortestDistances[vertexIndex] = Integer.MAX_VALUE;
            added[vertexIndex] = false;
        }

        // Distance of source vertex from
        // itself is always 0
        shortestDistances[startVertex] = 0;

        // Parent array to store shortest
        // path tree
        int[] parents = new int[nVertices];

        // The starting vertex does not
        // have a parent
        parents[startVertex] = NO_PARENT;

        // Find shortest path for all
        // vertices
        for (int i = 1; i < nVertices; i++)
        {

            // Pick the minimum distance vertex
            // from the set of vertices not yet
            // processed. nearestVertex is
            // always equal to startNode in
            // first iteration.
            int nearestVertex = -1;
            int shortestDistance = Integer.MAX_VALUE;
            for (int vertexIndex = 0;
                     vertexIndex < nVertices;
                     vertexIndex++)
            {
                if (!added[vertexIndex] &&
                    shortestDistances[vertexIndex] <
                    shortestDistance)
                {
                    nearestVertex = vertexIndex;
                    shortestDistance = shortestDistances[vertexIndex];
                }
            }

            // Mark the picked vertex as
            // processed
            added[nearestVertex] = true;

            // Update dist value of the
            // adjacent vertices of the
            // picked vertex.
            for (int vertexIndex = 0;
                     vertexIndex < nVertices;
                     vertexIndex++)
            {
                int edgeDistance = adjacencyMatrix[nearestVertex][vertexIndex];

                if (edgeDistance > 0
                    && ((shortestDistance + edgeDistance) <
                        shortestDistances[vertexIndex]))
                {
                    parents[vertexIndex] = nearestVertex;
                    shortestDistances[vertexIndex] = shortestDistance +
                                                       edgeDistance;
                }
            }
        }

        printSolution(startVertex, shortestDistances, parents);
    }

    // A utility function to print
    // the constructed distances
    // array and shortest paths
    private static void printSolution(int startVertex, int[] distances, int[] parents)
    {
        int nVertices = distances.length;
        String path="";
        for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++)
        {
            if (vertexIndex != startVertex)
            {
                best_path.put(vertexIndex, Integer.toString(parents[vertexIndex]));
                path = getLink(startVertex, vertexIndex, parents);
                best_path.put(vertexIndex, path);
            }
            path = "";
        }
        listOfMaps.add(startVertex, best_path);
        best_path = new HashMap<>();
    }


    private static String getLink(int src, int currentVertex, int[] parents){
        String path="";// = Integer.toString(currentVertex);
        while(currentVertex!=src){
            path += " "+Integer.toString(currentVertex);
            currentVertex = parents[currentVertex];
        }
        return path.substring(path.length() - 1);
    }


    public static void main(String args[]) {
        nodes = 9;
        parseAdjFile("pa2/adjacencyList.txt");
        initDistVect();
        int array[][] = new int[9][9];
        for(int i = 0; i < nodes; i++){
            // for(int j = 0; j < nodes; j++){
            //     array[i][j] = distVectors[i].dv[j];
            // }
            array[i] = distVectors[i].dv;
            System.out.println(Arrays.toString(array[i]));
        }
        for(int i = 0; i < nodes; i++){
            dijkstra(array, i);

        }
        System.out.println(listOfMaps.toString());
    }
}
