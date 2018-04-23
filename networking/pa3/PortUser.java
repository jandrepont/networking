package pa3;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class PortUser {
	int nodeId;
	DataOutputStream out;
	DataInputStream in;
	Socket soc = null;
	String ip;
	int port;

	public PortUser(int nodeId, String ip, int port) {
		this.nodeId = nodeId;
		this.ip = ip;
		this.port = port;
	}

	public void initialize() throws InterruptedException {
		while (soc == null) {
			try {
                soc = new Socket();
                soc.connect(new InetSocketAddress(ip, port));
				System.out.println("Got a socket.");
	            System.out.println("client soc IP:" + soc.getInetAddress());
	            System.out.println("client soc port:" + soc.getLocalPort());
	            System.out.println("client remote soc port:" + soc.getRemoteSocketAddress());
				in = new DataInputStream(soc.getInputStream());
				out = new DataOutputStream(soc.getOutputStream());
				out.writeInt(nodeId);
			} catch (UnknownHostException e) {
				System.err.println("Don't know about the server.");
			} catch (IOException e) {
                System.out.printf("ip = %s, port = %d", ip, port);
				System.err.println("Couldn't get I/O for the connection to server.");
			}
			Thread.sleep(1000);
		}
	}

	public MessageType receive() throws IOException {
		byte[] packet = new byte[1536];
		in.read(packet);
		MessageType msg = MessageType.bytearray2messagetype(packet);
		return msg;
	}

	public void send(MessageType msg) {
		byte[] packet = msg.toBytes();
		try {
			out.write(packet);
		} catch (SocketException se) {
			System.exit(1);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void close() throws IOException {
		try {
			System.out.println("Shutting down ...");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			in.close();
			out.close();
			soc.close();
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length != 3)
			System.out.println("usage: java PortUser server-ip server-port node-ID");
		int nodeId = Integer.parseInt(args[2]);
		String ip = args[0];
		int port = Integer.parseInt(args[1]);
		PortUser pu = new PortUser(nodeId, ip, port);
		pu.initialize();
		System.out.println("before sending");
		Thread.sleep(10000);
		byte[] pack = new byte[1024];
		MessageType msg = new MessageType(nodeId, 8, pack);
		for (int j = 0; j < 5; j++) {
			Arrays.fill(pack, (byte)j);
			System.out.println("sending: " + msg);
			try {
				pu.send(msg);
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
 		pu.close();
	}
}
