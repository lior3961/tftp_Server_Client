package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ServerActions;
import bgu.spl.net.srv.ServerData;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private int connectionId;
    private Connections<byte[]> connections;
    private ServerActions serverActions;
    private ServerData serverData;

    public TftpProtocol(ServerData serverData)
    {
        this.serverData = serverData;
    }

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
        this.serverActions = new ServerActions(this.serverData,this.connectionId);
    }

    @Override
    public void process(byte[] message)
    {
        byte [] b = new byte []{message[0] , message[1]};       
        short opCode = ( short ) ((( short ) b[0]) << 8 | ( short ) ( b[1]) );
        message = this.serverActions.act(opCode , message);
        if(message != null)
        {
            this.connections.send(this.connectionId, message);
            if(this.serverActions.getNeedToBcast() != -1)
            {
               this.serverData.sendBcast(serverActions.createBcastPacket(this.serverActions.getNeedToBcast()));
               this.serverActions.doneBcast();
            }
            System.out.println("Sent answer to client from proccess");
        }
    }

    @Override
    public boolean shouldTerminate() {
        return (this.serverData.hadlogOuted(this.connectionId));
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
