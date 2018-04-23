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
    static PortUser[] p_usr;

    public FWNode(int portNum, int networkSize, HashMap<Integer,String> fwd_table)
    {
        this.portNum = portNum;
        this.nNeighbor = networkSize;
        this.fwd_table = fwd_table;
        dplane = new DataPlanePort(portNum, nNeighbor);
        this.p_usr = new PortUser[nNeighbor];
        // dplane.initialize();
        // new Thread(dplane).start();
    }

    public void dp_init()
    {
        new Thread(dplane).start();
        // dplane.initialize(); 
    }

    public void p_usr_init()
    {
        for(int i = 0; i < nNeighbor; i++){
            PortUser pu;
            try{
                pu = p_usr[i];
                pu.initialize();
                p_usr[i] = pu;

            } catch(InterruptedException e){
                System.err.println(e);
            }
        }
    }



    public void set_p_usrs(PortUser[] p_usr)
    {
        this.p_usr = p_usr;
    }

    // public void initialize(){
    //     new Thread(dplane).start();
    //
    // }


}
