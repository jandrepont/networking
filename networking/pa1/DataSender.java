package pa2;
import java.net.*;
import java.net.Socket;
import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.*;

public class DataSender {
    public static void main(String[] args) throws IOException {

        if (args.length != 3) {
            System.err.println("Usage: java DataReceiver <port number> <interval sec> <num of different packets/sec>");
            System.exit(1);
        }
        int[] array = new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        int i, n, rate, j;
        ArrayList<Integer> frequency = new ArrayList<Integer>();
        int portNumber = Integer.parseInt(args[0]);
        int interval = Integer.parseInt(args[1]);
        interval = interval*1000; //sec to ms
        int n_frequencys = Integer.parseInt(args[2]);
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        for(i = 0; i < n_frequencys; i++ ){
            System.out.printf("How many packets per sec for frequency[%d] : ", i);
            n = reader.nextInt();
            frequency.add(n);
        }
        reader.close();

        try (
                ServerSocket serverSocket =
                        new ServerSocket(Integer.parseInt(args[0]));
                Socket clientSocket = serverSocket.accept();
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
        ) {
            System.out.println("Socket = " + clientSocket.getRemoteSocketAddress());
            String inputLine;
            long end;

            while (true) {
                for(i = 0; i < n_frequencys; i++){
                    rate = interval/frequency.get(i);
                    end = System.currentTimeMillis() + interval;
                    while((System.currentTimeMillis() < end)){
                        output.writeObject(array);
                        Thread.sleep((rate));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}