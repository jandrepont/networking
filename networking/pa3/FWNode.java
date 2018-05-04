package pa3;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.io.Serializable;
import java.lang.Thread;
class FWNode {

    int portNum;
    public int nNeighbor;
    HashMap<Integer, String> fwd_table;
    DataPlanePort dplane;
    static HashMap<Integer, PortUser> p_usr;
    static InetAddress myIp;
    static int nodeId;

    public FWNode(int portNum, int networkSize, HashMap<Integer,String> fwd_table, InetAddress ip, int node)
    {
        this.portNum = portNum;
        this.nNeighbor = networkSize;
        this.fwd_table = fwd_table;
        this.myIp = ip;
        this.nodeId = node;
        dplane = new DataPlanePort(portNum, nNeighbor, myIp);
        // this.p_usr = new PortUser[nNeighbor];
        // dplane.initialize();
        // new Thread(dplane).start();
    }

    public void dp_init()
    {
        new Thread(dplane).start();
    }

    public void p_usr_init(int key)
    {
        // for(Integer pls : p_usr.keySet()){
            try{
                p_usr.get(key).initialize();

            } catch(InterruptedException e){
                System.err.println(e);
            }
        // }
    }



    public void set_p_usrs(HashMap<Integer, PortUser> pls)
    {
        this.p_usr = pls;
        System.out.println(p_usr.toString());

    }

    public void sendNeighbor(MessageType message){
        int dest = message.getDestNode();
        int source = message.getSourceNode();
        System.out.printf("source Node: %d sending to Node: %d\n", source, dest);
        if(dest == nodeId){
            System.out.printf("Node: %d Received Message!\n", nodeId);
        } else {
            int fwdNode = (int)Integer.parseInt(fwd_table.get(dest));
            System.out.printf("Forwarding Message using node %d to reach node %d\n", fwdNode, dest);
            p_usr.get(fwdNode).send(message);
            // p_usr.get(dest).send(message);
        }
    }

    public void printConnections(){
        System.out.printf("My node # = %d\n", nodeId);
        System.out.printf("With portusers connected to: ");
        for(Integer key : p_usr.keySet()){
            System.out.printf("%d, ", key);
        }
        System.out.printf("\n");
        System.out.printf("With FWDTable = %s\n", fwd_table.toString());
    }

    public static void fwdMessage(MessageType message){

    }

    public MessageType receive()
    {
        MessageType message = null;
        try {
            message = dplane.receive();
        } catch(InterruptedException e){
            System.out.printf("Error w/ dplance.receive() for node %d\n", nodeId);
            e.printStackTrace();
        }
        return message;
    }



}
