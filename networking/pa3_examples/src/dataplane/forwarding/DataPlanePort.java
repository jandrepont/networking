package dataplane.forwarding;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;

public class DataPlanePort implements Runnable, Callback {
	int portNum;
	int nNeighbor;
	DataOutputStream[] outs ;
	Listner[] listners;
	ConcurrentLinkedQueue<MessageType> que;
	HashMap<Integer, Integer> neighbor2stream; 		// mapping node_num to outStream_num

	public DataPlanePort(int portNum, int networkSize) {
		this.portNum = portNum;
		this.nNeighbor = networkSize;
		outs = new DataOutputStream[nNeighbor];
		listners = new Listner[nNeighbor];
		que = new ConcurrentLinkedQueue<MessageType>();
		neighbor2stream = new HashMap<Integer, Integer>(nNeighbor);
	}

	public synchronized void initialize() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(portNum);
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = in.readLine();
            System.out.println(ip);
            // InetSocketAddress address = new InetSocketAddress(ip, portNum);
            // serverSocket.bind(address);
            System.out.println("server soc address:" + serverSocket.getInetAddress());
            System.out.println("server local socket soc address:" + serverSocket.getLocalSocketAddress());
            System.out.println("server soc port:" + serverSocket.getLocalPort());
        } catch (IOException ioe) {
        	ioe.printStackTrace();
        }
		for (int j = 0; j < nNeighbor; j++) {
			int nodeNum = -1;
       		try {
				Socket clientSocket = serverSocket.accept(); 	    // not part of communication
	            System.out.println("client soc IP:" + clientSocket.getInetAddress());
	            System.out.println("client soc port:" + clientSocket.getLocalPort());
	            System.out.println("client soc remote IP:" + clientSocket.getRemoteSocketAddress());
	            System.out.println("client soc remote port:" + clientSocket.getPort());
				outs[j] = new DataOutputStream(clientSocket.getOutputStream());
				DataInputStream in = new DataInputStream(clientSocket.getInputStream());
				nodeNum = in.readInt();
				neighbor2stream.put(nodeNum, j);
				listners[j] = new Listner(nodeNum, in, this);
			} catch (IOException ioe) {
				System.err.println("Failed in connection for j=" + j + "with " + nodeNum);
				ioe.printStackTrace();
				System.exit(-1);
			}
 		}
		System.out.println("Connections are all established.");
	}

	public void run() {
		initialize();
		for (int j = 0; j < nNeighbor; j++) {
			listners[j].start();
		}
	}

	public synchronized void callback(MessageType message) {
        // System.out.print(message);
		que.offer(message);
		notifyAll();
	}

	public synchronized MessageType receive() throws InterruptedException{
		while (que.isEmpty()) {
			wait();
		}
		MessageType msg = que.poll();
		System.out.println("receive: " + msg.getDestNode());
		return msg;
	}

	public int getPortNum() {
		return portNum;
	}

	public synchronized void setPortNum(int portNum) {
		this.portNum = portNum;
	}

	public int getnSize() {
		return nNeighbor;
	}

	public ConcurrentLinkedQueue<MessageType> getQue() {
		return que;
	}

	public static void main(String[] args) throws IOException, InterruptedException   {
		if (args.length != 2)
			System.out.println("usage: java DataPlanePort port-number number-of-nodes");
		int portNum = Integer.parseInt(args[0]);
		int numNode = Integer.parseInt(args[1]);
		DataPlanePort cp = new DataPlanePort(portNum, numNode);
		new Thread(cp).start();
		Thread.sleep(20000);
		System.out.println("SwitchPort: messages in queue:");
		Iterator<MessageType> ite = cp.getQue().iterator();
		while (ite.hasNext()) {
			System.out.println(ite.next());
		}
		System.out.println("Switch Port is done.");
	}
}

class Listner extends Thread {
	int pId;
	DataInputStream in;
	Callback requester;
	boolean done = false;
	final int ERR_THRESHOLD = 100;

	public Listner(int id, DataInputStream in, Callback requester) {
		this.pId = id;
		this.in = in;
		this.requester = requester;
	}

	public void run() {
		byte[] packet = new byte[1536];
		int msgSize = 0;
		while (msgSize != -1) {
			try {
				msgSize = in.read(packet);
				System.out.println("Listner " + pId + ": " + msgSize + " bytes.");
				MessageType msg = MessageType.bytearray2messagetype(packet);
                System.out.print(msg);
				requester.callback(msg);
			} catch (SocketException se) {
				System.err.println(se);
				System.exit(0);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
  		System.out.println("Listner " + pId + " is done." );
	}
}

interface Callback {
	public void callback(MessageType msg);
}
