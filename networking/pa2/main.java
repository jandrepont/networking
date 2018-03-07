package pa2;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class main {
    public static void main(String[] var0) {
       Connection con = new Connection();
       System.out.println(con.getLocalNotLoopbackIP());
    }
}
