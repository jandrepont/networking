package pa2;
import java.io.*;
import java.net.*;

class DVNode
{
    public static void main(String args[]) {

        String host = args[0];
        int port = Integer.parseInt(args[1]);

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

            DatagramSocket clientSocket = new DatagramSocket();
            ObjectStream stream = new ObjectStream();

            /*
             * Send ip Address
             */
            stream.sendObj(ip, host, port, clientSocket);

            /*
             * Receive datagram
             */
            int node = (int)stream.receiveObj(clientSocket);
            System.out.printf("Node # =" + node);
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Problem with server connecting");
        }
    }
}