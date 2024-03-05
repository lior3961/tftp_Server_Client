package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private int connectionId;
    private Connections<byte[]> connections;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(byte[] message)
    {
        if(message[1] == 1)
        {
            
        }
        if(message[1] == 2)
        {

        }
        if(message[1] == 3)
        {

        }
        if(message[1] == 4)
        {

        }
        if(message[1] == 5)
        {

        }
        if(message[1] == 6)
        {

        }
        if(message[1] == 7)
        {

        }
        if(message[1] == 8)
        {

        }
        if(message[1] == 9)
        {

        }
        if(message[1] == 10)
        {

        }
        
        
    }

    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        throw new UnsupportedOperationException("Unimplemented method 'shouldTerminate'");
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
