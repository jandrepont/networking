import java.io.*;
import java.net.*;
import java.lang.Math;
import java.util.Arrays;

public class DataReceiver {
    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println(
                    "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }


        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);


        try (
                Socket echoSocket = new Socket(hostName, portNumber);
                DataOutputStream output = new DataOutputStream(echoSocket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(echoSocket.getInputStream());
        ) {

            int[] array = (int[])ois.readObject();
            long beg, delta;
            while (true){
                beg = System.currentTimeMillis();
                ois.readObject();
                delta = System.currentTimeMillis() - beg;
                System.out.println("Input rate = " + delta);
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        } catch (ClassNotFoundException e){
            System.err.println("Class not found ");
            System.exit(1);

        }
    }
}