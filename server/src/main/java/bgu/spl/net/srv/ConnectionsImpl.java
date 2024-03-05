package bgu.spl.net.srv;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl implements Connections<byte[]> {

    private ConcurrentHashMap<Integer, ConnectionHandler<byte[]>> connections;

    public ConnectionsImpl()
    {
        connections = new ConcurrentHashMap<Integer, ConnectionHandler<byte[]>>();
    }

    @Override
    public void connect(int connectionId, ConnectionHandler<byte[]> handler)
    {
        this.connections.put(connectionId, handler);
    }

    @Override
    public boolean send(int connectionId, byte[] msg)
    {
        ConnectionHandler<byte[]> ch = this.connections.get(connectionId);
        if(ch != null)
        {
            ch.send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void disconnect(int connectionId)
    {
        this.connections.remove(connectionId);
    }
    
}
