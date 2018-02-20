/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.*;


public class DataSender {
    public static void main(String[] args) throws IOException {

        if (args.length != 4) {
            System.err.println(
                    "Usage: java EchoClient <host name> <port number> <interval sec> <num of different packets/sec>");
            System.exit(1);
        }

        int[] array = new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        int i, n, rate;
        ArrayList<Integer> frequency = new ArrayList<Integer>();
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        int interval = Integer.parseInt(args[2]);
        interval = interval*1000; //sec to ms
        int n_frequencys = Integer.parseInt(args[3]);
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        for(i = 0; i < n_frequencys; i++ ){
            System.out.printf("How many packets per sec for frequency[%d]", i);
            n = reader.nextInt();
            frequency.add(n);
        }
        reader.close();


        try (
                Socket echoSocket = new Socket(hostName, portNumber);
                PrintWriter out =
                        new PrintWriter(echoSocket.getOutputStream(), true);
                DataOutputStream output = new DataOutputStream(echoSocket.getOutputStream());
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(echoSocket.getInputStream()));
                BufferedReader stdIn =
                        new BufferedReader(
                                new InputStreamReader(System.in))
        ) {

            String userInput;
            PrintStream packet;
            long end;
            while (true) {
                for(i = 0; i < n_frequencys; i++){
                    rate = interval/frequency.get(i);
                    end = System.currentTimeMillis() + interval;
                    while(System.currentTimeMillis() < end){
                        output.writeBytes(Arrays.toString(array) + "frequency = " + Integer.toString(rate) + '\n');
                        Thread.sleep((rate/2));
                    }
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}