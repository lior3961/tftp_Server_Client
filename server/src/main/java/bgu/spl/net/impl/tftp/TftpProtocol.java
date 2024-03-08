package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ServerActions;
import bgu.spl.net.srv.ServerData;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private int connectionId;
    private Connections<byte[]> connections;
    private ServerActions action;
    private ServerData serverData;

    public TftpProtocol(ServerData serverData)
    {
        this.serverData = serverData;
    }

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
        this.action = new ServerActions(connections,this.serverData);
    }

    @Override
    public void process(byte[] message)
    {
        byte [] b = new byte []{message[0] , message[1]};
        short b_short = ( short ) ((( short ) b[0]) << 8 | ( short ) ( b[1]) );
        message = this.action.act(b_short , message);
        this.connections.send(this.connectionId, message); 
        System.out.println("Sent answer to client");

    }

    @Override
    public boolean shouldTerminate() {
        return (this.serverData.hadDisconnected(this.connectionId));
    } 

    public int getConnectionID()
    {
        return this.connectionId;
    }

    public Connections<byte[]> getConnection()
    {
        return this.connections;
    }

    
}
