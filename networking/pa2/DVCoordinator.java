package pa2;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
public class DVCoordinator extends Thread {

    public static void main(String args[]) {

        int node, n;
        n = 5;
        node = 0;
        HashMap<Integer, String> nodeIPtable = new HashMap<>();


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
            DatagramSocket server = new DatagramSocket(9876);
            ObjectStream stream = new ObjectStream();

            while(node < n)
            {
                /*
                 * Receive Datagram
                 */
                String nodeIP = (String)stream.receiveObj(server);
                System.out.printf("RECEIVED: %s from node: %d\n", nodeIP, node);

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
                    nodeIPtable.put(node, nodeIP);
                    /*
                     * Get credentials to send datagram to node
                     */
                    int port = stream.port;
                    stream.sendObj(node, nodeIP, port, server);
                    node++;
                }
            }
                server.close();
            System.out.println(nodeIPtable.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Problem with server connecting");
        }
    }
}
